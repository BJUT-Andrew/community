package leetcode;

/**
 * @author andrew
 * @create 2021-11-16 20:15
 */
public class Solution69 {
    public int mySqrt(int x) {
        int left = 0, right = x;
        int res = 1;

        while(left <= right){
            int mid = (left + right) / 2;
            if((long)mid * mid <= x){
                res = mid;
                left = mid + 1;
            }else{
                right = mid - 1;
            }
        }

        return res;
    }

    public static void main(String[] args) {
        int x = 9;
        Solution69 solution69 = new Solution69();
        System.out.println(solution69.mySqrt(x));
    }
}
