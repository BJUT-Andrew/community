package leetcode;

/**
 * @author andrew
 * @create 2021-11-19 11:27
 */
public class Solution912 {
    public int[] sortArray(int[] nums) {
        int n = nums.length;

        //建大顶堆
        heapify(nums);

        //循环将堆首元素交换到堆的末尾(有序数组的前一个位置)，并将新首元素下沉，调整成堆
        for (int i = n - 1; i > 0; ) {
            //将大顶堆的首元素交换到数组最后位置
            swap(nums, 0, i);
            i--;
            //首元素下沉
            siftdown(nums, 0, i);
        }
        return nums;
    }

    private void heapify(int[] nums) {

        int n = nums.length;
        //从最后一个有子结点的结点开始，依次向前，向后“下沉”
        for (int i = n / 2 - 1; i >= 0; i--) {
            //“下沉”
            siftdown(nums, i, n - 1);
        }
    }

    //某结点开始下沉的隐含条件：该结点孩子结点已经满足堆的标准；
    //(因为是从最后一个有孩子结点的结点开始的)
    //所以如果该结点值比孩子结点值都大，也就不用下沉；
    private void siftdown(int[] nums, int k, int heapEnd) {
        while (2 * k + 1 <= heapEnd) {
            //1.挑出该结点的孩子结点的中的较大值；
            int j = 2 * k + 1;
            if (j + 1 <= heapEnd && nums[j + 1] > nums[j])
                j++;

            //2.如果子节点较大值＞该结点值，则交换，并继续让k指向该结点；
            if (nums[j] > nums[k]) {
                swap(nums, j, k);
                k = j;
                //如果子节点较大值不大于该结点值，则该结点的下沉结束；
            } else {
                break;
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static void main(String[] args) {
        Solution912 solution912 = new Solution912();
        solution912.sortArray(new int[]{5,2,3,1});
    }
}
