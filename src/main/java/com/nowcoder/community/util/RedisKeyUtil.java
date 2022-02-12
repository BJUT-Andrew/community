package com.nowcoder.community.util;

/**
 * @author andrew
 * @create 2021-10-29 13:41
 */
public class RedisKeyUtil {

    //redis中key的单词分隔符
    private static final String SPLIT = ":";
    //redis中保存某实体的赞的key名字的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //某用户收到的赞的前缀
    private static final String PREFIX_USER_LIKE = "like:user";
    //一个关注，两份数据，关注方 and 被关注方
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    //验证码前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //登录凭证前缀
    private static final String PREFIX_TICKET = "ticket";
    //存储用户信息的KEY的前缀
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";


    //根据实体的type和id，拼接redis中存储其点赞用户id的key名字
    //为了后面方便查询哪个用户给此实体点过赞，redis中的value是一个存储点过赞的用户的userId的set集合
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某用户收到的赞的key
    //like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某用户关注的实体（可以是用户、帖子等...）
    //followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某实体的粉丝
    //follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //某用户的验证码的KEY
    //登录时随机生成一个字符串，用于标识目前要登录的用户
    public static String getKaptchKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //用户登录凭证的KEY
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //存储用户信息的KEY
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
