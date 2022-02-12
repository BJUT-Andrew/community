package leetcode;


import java.util.ArrayList;
import java.util.List;

/**
 * @author andrew
 * @create 2021-11-10 17:38
 */
class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }

    public ListNode insertionSortList(ListNode head) {

        ListNode dummy = new ListNode();
        dummy.next = head;

        if(head == null)
            return null;

        ListNode curr = head.next;
        ListNode prev = head;
        ListNode lastSorted = head;

        while(curr != null){

            if(curr.val >= lastSorted.val){
                lastSorted = lastSorted.next;
                prev = prev.next;
                curr = curr.next;
                break;
            }

            ListNode curr1 = dummy;
            while(curr1 != lastSorted){

                if(curr.val >= curr1.next.val){
                    curr1 = curr1.next;
                    continue;
                }

                prev.next = curr.next;
                curr.next = curr1.next;
                curr1.next = curr;
                curr = prev.next;
                lastSorted.next = curr;
                break;
            }
        }

        return dummy.next;
    }

    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(-1);
        ListNode listNode2 = new ListNode(5);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);
        ListNode listNode5 = new ListNode(0);

        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = listNode5;

        System.out.println(listNode1.insertionSortList(listNode1));
    }

}
