package me.pesekjak.hippo.preimport;

import ch.njol.skript.config.Config;

import java.util.HashMap;

public class PreImportManager {

    public final static PreImportManager MANAGER = new PreImportManager();
    private final HashMap<String, PreImporting> preImportingScripts;

    private PreImportManager() {
        this.preImportingScripts = new HashMap<>();
    }

    public void addPreImportingScript(String path) {
        this.preImportingScripts.putIfAbsent(path, new PreImporting());
        this.preImportingScripts.replace(path, new PreImporting());
    }

    public void addPreImportingScript(Config script) {
        addPreImportingScript(script.getFile().getPath());
    }

    public void removePreImportingScript(String path) {
        this.preImportingScripts.remove(path);
    }

    public void removePreImportingScript(Config script) {
        removePreImportingScript(script.getFile().getPath());
    }

    public PreImporting getPreImporting(String path) {
        return this.preImportingScripts.get(path);
    }

    public PreImporting getPreImporting(Config script) {
        return this.preImportingScripts.get(script.getFile().getPath());
    }

    public boolean isPreImporting(String path) {
        return this.preImportingScripts.containsKey(path);
    }

    public boolean isPreImporting(Config script) {
        return isPreImporting(script.getFile().getPath());
    }

    public void clear() {
        this.preImportingScripts.clear();
    }

    public static class PreImporting {
        private final HashMap<String, PreImport> preImportMap;

        public PreImporting() {
            this.preImportMap = new HashMap<>();
        }

        public void addPreImport(String alias, PreImport preImport) {
            preImportMap.putIfAbsent(alias, preImport);
            preImportMap.replace(alias, preImport);
        }

        public void removePreImport(String alias) {
            preImportMap.remove(alias);
        }

        public void removePreImport(PreImport preImport) {
            preImportMap.keySet().forEach((key) -> {
                if(preImportMap.get(key) == preImport) preImportMap.remove(key);
            });
        }

        public PreImport getPreImport(String alias) {
            return preImportMap.get(alias);
        }

        public String getAlias(PreImport preImport) {
            String found = null;
            for(String key : preImportMap.keySet()) {
                if(preImportMap.get(key) == preImport) found = key;
            }
            return found;
        }

        public boolean containsAlias(String alias) {
            return preImportMap.containsKey(alias);
        }

        public boolean containsPreImport(PreImport preImport) {
            return preImportMap.containsValue(preImport);
        }

        public void clear() {
            preImportMap.clear();
        }
    }

}
