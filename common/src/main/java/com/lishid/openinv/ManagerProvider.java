package com.lishid.openinv;

import com.lishid.openinv.util.lang.LanguageManager;

// TODO temp interface for minimal module migration diff, replace with dependency injection
public interface ManagerProvider {

  LanguageManager getLanguageManager();

}
