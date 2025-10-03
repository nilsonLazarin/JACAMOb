package neck.util;

import java.lang.reflect.*;
import java.util.*;

public final class ReflectCall {
    private ReflectCall() {}

//    public static Object invoke(Object target, String call) throws Exception {
//        String name = methodName(call);
//        List<String> argTokens = parseArgs(call);
//        Method m = findCompatibleMethod(target.getClass(), name, argTokens);
//        Object[] args = convertArgs(argTokens, m.getParameterTypes());
//        m.setAccessible(true);
//        return m.invoke(target, args);
//    }

    public static Object invoke(Object target, String call) throws Exception {
        String name = methodName(call);
        List<String> argTokens = parseArgs(call);
        try {
            // NOVO: tentar como construtor de classe (FQCN)
            ////////Class<?> cls = Class.forName(name); // ex.: "classe.do.usuario.que.extende.Apparatus"

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) cl = ReflectCall.class.getClassLoader();
            Class<?> cls = Class.forName(name, true, cl);

            Constructor<?> ctor = findCompatibleConstructor(cls, argTokens);
            Object[] args = convertArgs(argTokens, ctor.getParameterTypes());
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Constructor<?> findCompatibleConstructor(Class<?> cls, List<String> argTokens)
            throws NoSuchMethodException {
        Constructor<?> best = null;
        int n = argTokens.size();
        for (Constructor<?> c : cls.getConstructors()) {
            Class<?>[] pt = c.getParameterTypes();
            if (pt.length != n) continue;
            if (canConvertAll(argTokens, pt)) { best = c; break; }
        }
        if (best == null) throw new NoSuchMethodException(
                "Construtor compatível não encontrado: " + cls.getName() + "/" + n);
        return best;
    }


    private static String methodName(String call) {
        int p = call.indexOf('(');
        if (p < 0) throw new IllegalArgumentException("Chamada inválida: " + call);
        return call.substring(0, p).trim();
    }

    private static List<String> parseArgs(String call) {
        int p = call.indexOf('(');
        int q = call.lastIndexOf(')');
        if (p < 0 || q < p) throw new IllegalArgumentException("Chamada inválida: " + call);
        String inside = call.substring(p + 1, q).trim();
        if (inside.isEmpty()) return List.of();

        // split por vírgula respeitando aspas
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escape = false;

        for (int i = 0; i < inside.length(); i++) {
            char c = inside.charAt(i);
            if (escape) { cur.append(c); escape = false; continue; }
            if (c == '\\') { cur.append(c); escape = true; continue; }
            if (inQuotes) {
                cur.append(c);
                if (c == quoteChar) inQuotes = false;
            } else {
                if (c == '"' || c == '\'') { inQuotes = true; quoteChar = c; cur.append(c); }
                else if (c == ',') { parts.add(cur.toString().trim()); cur.setLength(0); }
                else { cur.append(c); }
            }
        }
        parts.add(cur.toString().trim());
        return parts;
    }

    private static Method findCompatibleMethod(Class<?> cls, String name, List<String> argTokens) throws NoSuchMethodException {
        Method best = null;
        int n = argTokens.size();
        for (Method m : cls.getMethods()) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length != n) continue;
            if (canConvertAll(argTokens, pt)) { best = m; break; }
        }
        if (best == null) throw new NoSuchMethodException("Método compatível não encontrado: " + name + "/" + n);
        return best;
    }

    private static boolean canConvertAll(List<String> toks, Class<?>[] types) {
        try { convertArgs(toks, types); return true; }
        catch (Exception e) { return false; }
    }

    private static Object[] convertArgs(List<String> toks, Class<?>[] types) {
        Object[] out = new Object[types.length];
        for (int i = 0; i < types.length; i++) out[i] = convert(toks.get(i), types[i]);
        return out;
    }

    private static Object convert(String tok, Class<?> type) {
        tok = tok.trim();
        if (type == String.class) {
            if ((tok.startsWith("\"") && tok.endsWith("\"")) || (tok.startsWith("'") && tok.endsWith("'"))) {
                return tok.substring(1, tok.length() - 1);
            }
            return tok; // sem aspas: trata como literal bruto
        }
        if (type == int.class || type == Integer.class) return Integer.valueOf(stripSuffix(tok, 'i'));
        if (type == long.class || type == Long.class) return tok.matches(".*[lL]$") ? Long.valueOf(tok.substring(0, tok.length()-1)) : Long.valueOf(tok);
        if (type == double.class || type == Double.class) return Double.valueOf(tok);
        if (type == float.class || type == Float.class) return Float.valueOf(tok);
        if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(tok);
        if (type == char.class || type == Character.class) {
            if ((tok.startsWith("'") && tok.endsWith("'")) && tok.length() >= 3) return tok.charAt(1);
            throw new IllegalArgumentException("Char inválido: " + tok);
        }
        // outros tipos: poderia usar JSON ou um conversor próprio
        throw new IllegalArgumentException("Conversão não suportada para: " + type.getName());
    }

    private static String stripSuffix(String s, char suf) {
        return (s.length() > 1 && (s.charAt(s.length()-1) == suf || s.charAt(s.length()-1) == Character.toUpperCase(suf)))
                ? s.substring(0, s.length()-1) : s;
    }
}
