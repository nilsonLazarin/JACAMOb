// EnvCompilerLite.java
package group.chon.jacamoB;

import cartago.Workspace;

import javax.tools.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public final class EnvCompilerLite {
    static volatile boolean ENV_CL_INSTALLED = false;

    private EnvCompilerLite() {}

    public static void compileAndInstall(String path) {
        try {
            Path src = Paths.get(path);
            if (!Files.isDirectory(src)) return;

            List<File> sources;
            try (var s = Files.walk(src)) {
                sources = s.filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toFile).collect(Collectors.toList());
            }
            if (sources.isEmpty()){
                return;
            }else{
                for (File source : sources) {
                    System.out.println("Loading Env File: " + source.getAbsolutePath());
                }
            }

            Path out = Paths.get("target/env-classes");
            Files.createDirectories(out);

            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            if (jc == null){
                System.out.println("No Java System Java Compiler");
                throw new IllegalStateException("Rode com um JDK (javac indisponível).");
            }else{
                System.out.println("Using Java System Java Compiler");
            }

            try (var fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
                fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(out.toFile()));
                var opts = List.of("-classpath", System.getProperty("java.class.path"));
                var units = fm.getJavaFileObjectsFromFiles(sources);
                boolean ok = jc.getTask(null, fm, null, opts, null, units).call();
                if (!ok) throw new IllegalStateException("Falha ao compilar fontes de src/env");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            URLClassLoader envCl = new URLClassLoader(new URL[]{ out.toUri().toURL() },
                    ClassLoader.getSystemClassLoader());
            Thread.currentThread().setContextClassLoader(envCl);
        } catch (Exception e) {
            throw new RuntimeException("Erro preparando ambiente: " + e.getMessage(), e);
        }
    }

    public static void ensureEnvClassLoaderInstalled() {
        if (ENV_CL_INSTALLED) return;
        synchronized (Workspace.class) {
            if (ENV_CL_INSTALLED) return;
            try {
                var out = java.nio.file.Paths.get("target/env-classes");
                if (java.nio.file.Files.isDirectory(out)) {
                    var url = out.toUri().toURL();
                    // parent = *o mesmo loader da app* (para ver JaCaMo/Cartago)
                    ClassLoader parent = ClassLoader.getSystemClassLoader();
                    var envCl = new java.net.URLClassLoader(new java.net.URL[]{url}, parent);
                    Thread.currentThread().setContextClassLoader(envCl);
                    ENV_CL_INSTALLED = true;
                    System.out.println("EnvClassLoader instalado: " + url);
                } else {
                    System.out.println("Aviso: target/env-classes não existe.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Falha instalando EnvClassLoader", e);
            }
        }
    }

}
