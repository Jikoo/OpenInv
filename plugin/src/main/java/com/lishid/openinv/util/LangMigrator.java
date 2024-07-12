package com.lishid.openinv.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LangMigrator {

  private final @NotNull Path oldFolder;
  private final @NotNull Path newFolder;
  private final @NotNull Logger logger;

  public LangMigrator(@NotNull Path oldFolder, @NotNull Path newFolder, @NotNull Logger logger) {
    this.oldFolder = oldFolder;
    this.newFolder = newFolder;
    this.logger = logger;
  }

  public void migrate() {
    if (!Files.exists(oldFolder.resolve("en_us.yml"))) {
      // Probably already migrated.
      return;
    }

    logger.info(() -> String.format("[LanguageManager] Migrating language files to %s", newFolder));

    if (!Files.exists(newFolder)) {
      try {
        Files.createDirectories(newFolder);
      } catch (IOException e) {
        logger.log(Level.WARNING, "Unable to create language subdirectory!", e);
      }
    }

    try (DirectoryStream<Path> files = Files.newDirectoryStream(oldFolder)) {
      files.forEach(path -> {
        if (path == null) {
          return;
        }

        String fileName = path.getFileName().toString();

        if (fileName.startsWith("config") || !fileName.endsWith(".yml")) {
          return;
        }

        // Migrate certain files to be parent languages.
        fileName = switch (fileName) {
          case "en_us.yml" -> "en.yml";
          case "de_de.yml" -> "de.yml";
          case "es_es.yml" -> "es.yml";
          case "pt_br.yml" -> "pt.yml";
          default -> fileName;
        };

        try {
          Files.copy(path, newFolder.resolve(fileName));
          Files.delete(path);
        } catch (FileAlreadyExistsException e1) {
          // File already migrated?
          try {
            Files.copy(path, newFolder.resolve("old_" + fileName));
            Files.delete(path);
          } catch (IOException e2) {
            // If it fails again, just re-throw.
            throw new UncheckedIOException(e2);
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException e) {
      logger.log(Level.WARNING, "Unable to migrate languages to subdirectory!", e.getCause());
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to migrate languages to subdirectory!", e);
    }

  }

}
