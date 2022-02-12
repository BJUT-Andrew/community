package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 *
 * @author andrew
 * @create 2021-10-24 11:25
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    //创建一棵前缀树（根结点）
    private TrieNode rootNode = new TrieNode();

    //定义前缀树的数据结构
    private class TrieNode {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子结点(key是子结点中存放的字符值，value是子节点（指针）)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode trieNode) {
            this.subNodes.put(c, trieNode);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    //将一个敏感词字符串添加到此过滤器的前缀树中
    public void addKeyword(String keyword) {

        TrieNode tempNode = rootNode;

        //遍历当前前缀树，无字符为keyword.charAt(i)的子结点则为其添加此子结点
        for (int i = 0; i < keyword.length(); i++) {

            TrieNode subNode = tempNode.getSubNode(keyword.charAt(i));

            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(keyword.charAt(i), subNode);
            }

            //遍历前缀树，向下一层添加
            tempNode = subNode;

            //当前字符串添加完毕，标识最后一个结点为end
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //根据敏感词，初始化过滤器的前缀树
    //@PostConstruct注解：标明此方法是初始化方法，让其在此类的构造器被调用时执行；
    @PostConstruct
    public void init() {

        try (
                //输入流，读取txt配置文件
             InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");

             //缓冲输出流
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //将每一行的敏感词添加到前缀树中
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 过滤敏感词算法；
     * @param text 待过滤文本
     * @return 过滤后文本
     */
    public String filter(String text){

        if (StringUtils.isBlank(text)){
            return null;
        }

        //遍历前缀树指针
        TrieNode tempNode = rootNode;
        //遍历待过滤文本双指针
        int begin = 0;
        int position = 0;

        //存放过滤后结果字符串
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == rootNode) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = rootNode;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }

        return sb.toString();
    }
}
