package com.nowcoder.community.entity;

/**
 * @author andrew
 * @create 2021-10-14 14:11
 */
public class Page {

    //当前页数
    private int current = 1;
    //一页的帖子上限
    private int limit = 10;
    //帖子总数（用于计算总页数）
    private int rows;
    //查询路径（点击页数直接跳转的请求路径）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0){
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行。
     * @return
     */
    public int getOffset(){
        return (current - 1) * limit;
    }

    /**
     * 获取总页数。
     * @return
     */
    public int getTotal(){
        return (rows % limit == 0) ? (rows / limit) : ((rows / limit) + 1);
    }

    /**
     * 获取当前显示的页数的起始页码：首页1 2 3 4 5末页
     * @return
     */
    public int getFrom(){
        int from  = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取当前显示的页数的结束页码:首页5 6 7 8 9末页
     * @return
     */
    public int getTo(){
        int to = current + 2;
        return to > getTotal() ? getTotal() : to;
    }

}
