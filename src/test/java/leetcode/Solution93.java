package leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andrew
 * @create 2021-11-16 19:28
 */
public class Solution93 {

    int SEG_COUNT = 4;
    List<String> res = new ArrayList<>();
    //存每次确定的IP地址的数字
    int[] oneRes = new int[SEG_COUNT];

    public List<String> restoreIpAddresses(String s) {
        oneRes = new int[SEG_COUNT];
        dfs(s, 0, 0);
        return res;
    }

    //segId：本次递归要确定的IP段号，取0 1 2 3；
    //segStart：本次递归确定IP段后时，从字符串s的此位开始；
    private void dfs(String s, int segId, int segStart) {
        //1.递归终止条件；
        //如果IP的4段都已确定 and 字符串遍历完毕，则处理本次确定的结果，添加到结果集中；
        if (segId == SEG_COUNT) {
            if (segStart == s.length()) {
                StringBuffer oneResStringBuffer = new StringBuffer();

                for (int i = 0; i < oneRes.length; i++) {
                    oneResStringBuffer.append(oneRes[i]);
                    if (i != oneRes.length - 1) {
                        oneResStringBuffer.append('.');
                    }
                }
                res.add(oneResStringBuffer.toString());
            }
            //IP4段都已确定，但是字符串未遍历完毕，说明此次结果不正确；
            return;
        }

        //IP的4段还未全部确定，就已经把字符串s遍历完了，说明此次结果不正确；
        if (segStart == s.length())
            return;

        //3.递归进入下一层；
        //如果此段开始就是0，则此段只能是0，因为IP地址不能有前导0，进入下一层；
        if (s.charAt(segStart) == '0') {
            oneRes[segId] = 0;
            dfs(s, segId + 1, segStart + 1);
        }

        //★ 一般情况，枚举每一种可能性并递归进入下一层；
        int addr = 0;
        for (int segEnd = segStart; segEnd < s.length(); segEnd++) {
            addr = addr * 10 + (s.charAt(segEnd) - '0');

            if (addr > 0 && addr <= 255) {
                oneRes[segId] = addr;
                dfs(s, segId + 1, segEnd + 1);
            } else {
                break;
            }
        }

    }

    public static void main(String[] args) {
        Solution93 solution93 = new Solution93();
        String s = "25525511135";
        System.out.println(solution93.restoreIpAddresses(s));
    }
}
