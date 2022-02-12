package leetcode;

/**
 * @author andrew
 * @create 2021-11-18 18:47
 */
public class Solution43 {
    public String multiply(String num1, String num2) {

        if (num1.equals("0") || num2.equals("0")) {
            return "0";
        }

        String res = "0";

        for (int i = num1.length() - 1; i >= 0; i--) {
            StringBuffer temp = new StringBuffer();
            int x = num1.charAt(i) - '0';
            int carry = 0;

            //提前补0，到时候reverse，0就到后面了
            for (int j = 1; j <= num1.length() - 1 - i; j++) {
                temp.append(0);
            }

            //得到的temp是反向存当前乘数结果的
            for (int j = num2.length() - 1; j >= 0; j--) {
                int y = num2.charAt(j) - '0';
                int tempRes = x * y + carry;
                temp.append(tempRes % 10);
                carry = tempRes / 10;
            }

            //进位可能不为0
            if (carry != 0)
                temp.append(carry);

            res = addStrings(res, temp.reverse().toString());
        }
        return res;
    }

    public String addStrings(String num1, String num2) {
        if (num1.equals("0"))
            return num2;
        if (num2.equals("0"))
            return num1;

        int carry = 0;
        StringBuffer reverseResSb = new StringBuffer();
        int m = num1.length(), n = num2.length();

        for (int i = m - 1, j = m - 1; i >= 0 || j >= 0; i--, j--) {
            int x = i >= 0 ? num1.charAt(i) - '0' : 0;
            int y = j >= 0 ? num2.charAt(j) - '0' : 0;

            int num = x + y + carry;

            reverseResSb.append(num % 10);
            carry = num / 10;
        }

        if (carry != 0)
            reverseResSb.append(carry);

        return reverseResSb.reverse().toString();
    }

    public static void main(String[] args) {
        Solution43 solution43 = new Solution43();
        System.out.println(solution43.multiply("98", "9"));
    }
}
