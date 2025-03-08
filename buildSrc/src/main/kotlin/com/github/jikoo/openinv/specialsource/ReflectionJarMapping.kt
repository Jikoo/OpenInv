package com.github.jikoo.openinv.specialsource

import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.ProgressMeter
import net.md_5.specialsource.transformer.MappingTransformer
import net.md_5.specialsource.transformer.MavenShade
import org.objectweb.asm.commons.Remapper
import java.io.BufferedReader
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern


open class ReflectionJarMapping : JarMapping() {

  val reflectableFields: MutableMap<String, String> = HashMap()
  protected var currentClass: String? = null

  override fun loadMappings(
    reader: BufferedReader,
    inputTransformer: MappingTransformer?,
    outputTransformer: MappingTransformer?,
    reverse: Boolean
  ) {
    val inTransformer = inputTransformer ?: MavenShade.IDENTITY
    val outTransformer = outputTransformer ?: MavenShade.IDENTITY

    val lines = reader.lines()
      .map {
        val comment = it.indexOf('#')
        if (comment == -1) {
          return@map it
        }
        return@map it.substring(0, comment)
      }
      .filter(String::isNotBlank)
      .toList()

    val meter = ProgressMeter(lines.size * 2, "Loading mappings... %2.0f%%")
    val clsMap: MutableMap<String, String> = HashMap()
    val prgMap: MutableMap<String, String> = HashMap()

    val proguard = " -> ".toRegex()
    for (l in lines) {
      if (l.endsWith(":")) {
        val parts = l.split(proguard).dropLastWhile(String::isEmpty)
        val orig = parts[0].replace('.', '/')
        val obf = parts[1].substring(0, parts[1].length - 1).replace('.', '/')
        clsMap[obf] = orig
        prgMap[orig] = obf
      } else if (l.contains(":")) {
        if (!l.startsWith("CL:")) {
          continue
        }

        val tokens = l.split(" ".toRegex()).dropLastWhile(String::isEmpty)
        clsMap[tokens[0]] = tokens[1]
      } else {
        if (l.startsWith("\t")) {
          continue
        }

        val tokens = l.split(" ".toRegex()).dropLastWhile(String::isEmpty)
        if (tokens.size == 2) {
          clsMap[tokens[0]] = tokens[1]
        }
      }

      meter.makeProgress()
    }

    val reverseMapper: Remapper = object : Remapper() {
      override fun map(cls: String): String {
        return clsMap.getOrDefault(cls, cls)
      }
    }

    for (l in lines) {
      if (!l.startsWith("tsrg2")) {
        if (l.contains(" -> ")) {
          this.parseProguardLine(l, inTransformer, outTransformer, reverse, reverseMapper, prgMap)
        } else if (l.contains(":")) {
          this.parseSrgLine(l, inTransformer, outTransformer, reverse)
        } else {
          this.parseCsrgLine(l, inTransformer, outTransformer, reverse, reverseMapper)
        }

        meter.makeProgress()
      }
    }

    this.currentClass = null
  }

  class ProguardUtil {
    companion object {
      val MEMBER_PATTERN: Pattern = Pattern.compile("(?:\\d+:\\d+:)?(.*?) (.*?) -> (.*)")

      fun csrgDesc(data: Map<String, String>, args: String, ret: String): String {
        val parts = args.substring(1, args.length - 1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val desc = StringBuilder("(")
        for (part in parts) {
          if (part.isEmpty()) {
            continue
          }
          desc.append(toJvmType(data, part))
        }
        desc.append(")")
        desc.append(toJvmType(data, ret))
        return desc.toString()
      }

      fun toJvmType(data: Map<String, String>, type: String): String {
        when (type) {
          "byte" -> return "B"
          "char" -> return "C"
          "double" -> return "D"
          "float" -> return "F"
          "int" -> return "I"
          "long" -> return "J"
          "short" -> return "S"
          "boolean" -> return "Z"
          "void" -> return "V"
          else -> {
            if (type.endsWith("[]")) {
              return "[" + toJvmType(data, type.substring(0, type.length - 2))
            }
            val clazzType = type.replace('.', '/')
            val mappedType = data[clazzType]

            return "L" + (mappedType ?: clazzType) + ";"
          }
        }
      }
    }

  }

  @Throws(IOException::class)
  open fun parseProguardLine(
    originalLine: String,
    inputTransformer: MappingTransformer,
    outputTransformer: MappingTransformer,
    reverse: Boolean,
    reverseMap: Remapper,
    prgMap: Map<String, String>
  ) {
    // Tsrg format, identical to Csrg, except the field and method lines should use the last class the was parsed.
    var line = originalLine
    if (line.startsWith("    ")) {
      if (this.currentClass == null) {
        throw IOException("Invalid proguard file, tsrg field/method line before class line: $line")
      }
      line = line.trim { it <= ' ' }
    }

    if (line.endsWith(":")) {
      val parts = line.split(" -> ".toRegex()).dropLastWhile { it.isEmpty() }
      val orig = parts[0].replace('.', '/')
      val obf = parts[1].substring(0, parts[1].length - 1).replace('.', '/')

      val oldClassName = inputTransformer.transformClassName(obf)
      val newClassName = outputTransformer.transformClassName(orig)

      if (oldClassName.endsWith("/")) {
        // Special case: mapping an entire hierarchy of classes
        if (reverse) {
          packages[newClassName] = oldClassName.substring(0, oldClassName.length - 1)
        } else {
          packages[oldClassName.substring(0, oldClassName.length - 1)] = newClassName
        }
      } else {
        if (reverse) {
          classes[newClassName] = oldClassName
        } else {
          classes[oldClassName] = newClassName
        }
        currentClass = obf
      }
    } else {
      val matcher: Matcher = ProguardUtil.MEMBER_PATTERN.matcher(line)
      matcher.find()

      val obfName: String = matcher.group(3)
      val nameDesc: String = matcher.group(2)
      if (nameDesc.contains("(")) {
        val desc = ProguardUtil.csrgDesc(prgMap, nameDesc.substring(nameDesc.indexOf('(')), matcher.group(1))
        val newName = nameDesc.substring(0, nameDesc.indexOf('('))

        var oldClassName = inputTransformer.transformClassName(currentClass)
        var oldMethodName = inputTransformer.transformMethodName(currentClass, obfName, desc)
        var oldMethodDescriptor = inputTransformer.transformMethodDescriptor(desc)
        var newMethodName = outputTransformer.transformMethodName(currentClass, newName, desc)

        if (reverse) {
          val newClassName = reverseMap.map(oldClassName)
          oldClassName = newClassName
          oldMethodDescriptor = reverseMap.mapMethodDesc(oldMethodDescriptor)

          val temp = newMethodName
          newMethodName = oldMethodName
          oldMethodName = temp
        }

        methods["$oldClassName/$oldMethodName $oldMethodDescriptor"] = newMethodName
      } else {
        val desc = ProguardUtil.toJvmType(prgMap, matcher.group(1))

        var oldClassName = inputTransformer.transformClassName(currentClass)
        var oldFieldName = inputTransformer.transformFieldName(currentClass, obfName)
        var oldFieldDesc = inputTransformer.transformMethodDescriptor(desc)
        var newFieldName = outputTransformer.transformFieldName(currentClass, nameDesc)

        if (reverse) {
          val newClassName = reverseMap.map(oldClassName)
          oldClassName = newClassName
          oldFieldDesc = reverseMap.mapDesc(oldFieldDesc)

          val temp = newFieldName
          newFieldName = oldFieldName
          oldFieldName = temp
        }

        reflectableFields["$oldClassName/$oldFieldName"] = newFieldName
        fields["$oldClassName/$oldFieldName/$oldFieldDesc"] = newFieldName
      }
    }
  }

  /**
   * Parse a 'csrg' mapping format line and populate the data structures
   */
  @Throws(IOException::class)
  open fun parseCsrgLine(
    originalLine: String,
    inputTransformer: MappingTransformer,
    outputTransformer: MappingTransformer,
    reverse: Boolean,
    reverseMap: Remapper
  ) {
    //Tsrg format, identical to Csrg, except the field and method lines start with \t and should use the last class the was parsed.
    var line = originalLine
    if (line.startsWith("\t\t")) {
      // Two tabs means the format is Tsrgv2 with parameters and extra data that isn't needed.
      return
    }
    if (line.startsWith("\t")) {
      if (this.currentClass == null) {
        throw IOException("Invalid tsrg file, tsrg field/method line before class line: $line")
      }
      line = currentClass + " " + line.substring(1)
    }

    val tokens = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }

    if (tokens.size == 2) {
      var oldClassName = inputTransformer.transformClassName(tokens[0])
      var newClassName = outputTransformer.transformClassName(tokens[1])

      if (oldClassName.endsWith("/")) {
        // package names always either 1) suffixed with '/', or 2) equal to '.' to signify default package

        if (newClassName != "." && !newClassName.endsWith("/")) {
          newClassName += "/"
        }

        if (oldClassName != "." && !oldClassName.endsWith("/")) {
          oldClassName += "/"
        }

        // Special case: mapping an entire hierarchy of classes
        if (reverse) {
          packages[newClassName] = oldClassName
        } else {
          packages[oldClassName] = newClassName
        }
      } else {
        if (reverse) {
          classes[newClassName] = oldClassName
        } else {
          classes[oldClassName] = newClassName
        }
        currentClass = tokens[0]
      }
    } else if (tokens.size == 3) {
      var oldClassName = inputTransformer.transformClassName(tokens[0])
      var oldFieldName = inputTransformer.transformFieldName(tokens[0], tokens[1])
      var newFieldName = outputTransformer.transformFieldName(tokens[0], tokens[2])

      if (reverse) {
        val newClassName = reverseMap.map(oldClassName)
        oldClassName = newClassName

        val temp = newFieldName
        newFieldName = oldFieldName
        oldFieldName = temp
      }

      reflectableFields["$oldClassName/$oldFieldName"] = newFieldName
      fields["$oldClassName/$oldFieldName"] = newFieldName
    } else if (tokens.size == 4) {
      var oldClassName = inputTransformer.transformClassName(tokens[0])
      var oldMethodName = inputTransformer.transformMethodName(tokens[0], tokens[1], tokens[2])
      var oldMethodDescriptor = inputTransformer.transformMethodDescriptor(tokens[2])
      var newMethodName = outputTransformer.transformMethodName(tokens[0], tokens[3], tokens[2])

      if (reverse) {
        val newClassName = reverseMap.map(oldClassName)
        oldClassName = newClassName
        oldMethodDescriptor = reverseMap.mapMethodDesc(oldMethodDescriptor)

        val temp = newMethodName
        newMethodName = oldMethodName
        oldMethodName = temp
      }

      methods["$oldClassName/$oldMethodName $oldMethodDescriptor"] = newMethodName
    } else {
      throw IOException("Invalid csrg file line, token count " + tokens.size + " unexpected in " + line)
    }
  }

  /**
   * Parse a standard 'srg' mapping format line and populate the data
   * structures
   */
  @Throws(IOException::class)
  open fun parseSrgLine(
    line: String,
    inputTransformer: MappingTransformer,
    outputTransformer: MappingTransformer,
    reverse: Boolean
  ) {
    val tokens = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
    val kind = tokens[0]

    if (kind == "CL:") {
      var oldClassName = inputTransformer.transformClassName(tokens[1])
      var newClassName = outputTransformer.transformClassName(tokens[2])

      if (reverse) {
        val temp = newClassName
        newClassName = oldClassName
        oldClassName = temp
      }

      require(!(classes.containsKey(oldClassName) && newClassName != classes[oldClassName])) {
        ("Duplicate class mapping: " + oldClassName + " -> " + newClassName
            + " but already mapped to " + classes[oldClassName] + " in line=" + line)
      }

      if (oldClassName.endsWith("/*") && newClassName.endsWith("/*")) {
        // extension for remapping class name prefixes
        oldClassName = oldClassName.substring(0, oldClassName.length - 1)
        newClassName = newClassName.substring(0, newClassName.length - 1)

        packages[oldClassName] = newClassName
      } else {
        classes[oldClassName] = newClassName
        currentClass = tokens[0]
      }
    } else if (kind == "PK:") {
      var oldPackageName = inputTransformer.transformClassName(tokens[1])
      var newPackageName = outputTransformer.transformClassName(tokens[2])

      if (reverse) {
        val temp = newPackageName
        newPackageName = oldPackageName
        oldPackageName = temp
      }

      // package names always either 1) suffixed with '/', or 2) equal to '.' to signify default package
      if (newPackageName != "." && !newPackageName.endsWith("/")) {
        newPackageName += "/"
      }

      if (oldPackageName != "." && !oldPackageName.endsWith("/")) {
        oldPackageName += "/"
      }

      require(!(packages.containsKey(oldPackageName) && newPackageName != packages[oldPackageName])) {
        ("Duplicate package mapping: " + oldPackageName + " ->" + newPackageName
            + " but already mapped to " + packages[oldPackageName] + " in line=" + line)
      }

      packages[oldPackageName] = newPackageName
    } else if (kind == "FD:") {
      val oldFull = tokens[1]
      val newFull = tokens[2]

      // Split the qualified field names into their classes and actual names
      val splitOld = oldFull.lastIndexOf('/')
      val splitNew = newFull.lastIndexOf('/')
      require(!(splitOld == -1 || splitNew == -1)) {
        ("Field name is invalid, not fully-qualified: " + oldFull
            + " -> " + newFull + " in line=" + line)
      }

      var oldClassName = inputTransformer.transformClassName(oldFull.substring(0, splitOld))
      var oldFieldName =
        inputTransformer.transformFieldName(oldFull.substring(0, splitOld), oldFull.substring(splitOld + 1))
      val newClassName = outputTransformer.transformClassName(
        newFull.substring(
          0,
          splitNew
        )
      )
      var newFieldName =
        outputTransformer.transformFieldName(oldFull.substring(0, splitOld), newFull.substring(splitNew + 1))

      if (reverse) {
        oldClassName = newClassName

        val temp = newFieldName
        newFieldName = oldFieldName
        oldFieldName = temp
      }

      val oldEntry = "$oldClassName/$oldFieldName"
      require(!(fields.containsKey(oldEntry) && newFieldName != fields[oldEntry])) {
        ("Duplicate field mapping: " + oldEntry + " ->" + newFieldName
            + " but already mapped to " + fields[oldEntry] + " in line=" + line)
      }

      reflectableFields[oldEntry] = newFieldName
      fields[oldEntry] = newFieldName
    } else if (kind == "MD:") {
      val oldFull = tokens[1]
      val newFull = tokens[3]

      // Split the qualified field names into their classes and actual names
      val splitOld = oldFull.lastIndexOf('/')
      val splitNew = newFull.lastIndexOf('/')
      require(!(splitOld == -1 || splitNew == -1)) {
        ("Field name is invalid, not fully-qualified: " + oldFull
            + " -> " + newFull + " in line=" + line)
      }

      var oldClassName = inputTransformer.transformClassName(oldFull.substring(0, splitOld))
      var oldMethodName = inputTransformer.transformMethodName(
        oldFull.substring(0, splitOld), oldFull.substring(splitOld + 1),
        tokens[2]
      )
      var oldMethodDescriptor = inputTransformer.transformMethodDescriptor(tokens[2])
      val newClassName = outputTransformer.transformClassName(
        newFull.substring(
          0,
          splitNew
        )
      )
      var newMethodName = outputTransformer.transformMethodName(
        oldFull.substring(0, splitOld), newFull.substring(splitNew + 1),
        tokens[2]
      )
      val newMethodDescriptor =
        outputTransformer.transformMethodDescriptor(tokens[4])

      if (reverse) {
        oldClassName = newClassName
        oldMethodDescriptor = newMethodDescriptor

        val temp = newMethodName
        newMethodName = oldMethodName
        oldMethodName = temp
      }

      val oldEntry = "$oldClassName/$oldMethodName $oldMethodDescriptor"
      require(!(methods.containsKey(oldEntry) && newMethodName != methods[oldEntry])) {
        ("Duplicate method mapping: " + oldEntry + " ->" + newMethodName
            + " but already mapped to " + methods[oldEntry] + " in line=" + line)
      }

      methods[oldEntry] = newMethodName
    } else {
      throw IllegalArgumentException("Unable to parse srg file, unrecognized mapping type in line=$line")
    }
  }

}
