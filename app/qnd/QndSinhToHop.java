package qnd;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class QndSinhToHop {

    static void sinhToHop(String[] kwList) {
        for (int len = 1; len <= kwList.length; len++) {
            sinhToHop(kwList, len);
        }
    }

    static void sinhToHop(String[] kwList, int len) {
        Set<String> check = new HashSet<String>();
        String[] buff = new String[len];
        for (int pos = 0; pos < len; pos++) {
            sinhToHop(kwList, len, pos, check, buff);
        }
    }

    static void sinhToHop(String[] kwList, int len, int pos, Set<String> check, String[] buff) {
        for (String kw : kwList) {
            if (check.contains(kw)) {
                continue;
            }
            buff[pos] = kw;
            check.add(kw);
            if (pos < len - 1) {
                sinhToHop(kwList, len, pos + 1, check, buff);
            } else {
                System.out.println(StringUtils.join(buff, " "));
            }
            check.remove(kw);
        }
    }

    static Set<String> toSet(String[] strList) {
        Set<String> result = new HashSet<String>();
        for (String str : strList) {
            result.add(str);
        }
        return result;
    }

    public static void main(String... args) {
        final String[] KW_LIST = { "a", "b" };
        sinhToHop(KW_LIST);
    }

}
