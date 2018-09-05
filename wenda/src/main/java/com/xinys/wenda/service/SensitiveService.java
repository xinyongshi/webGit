package com.xinys.wenda.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤操作
 */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    private static final String DEFAULT_REPLACEMENT = "***";

    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();
        try{
            InputStream is = Thread.currentThread().
                    getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String lineTxt;
            while((lineTxt = reader.readLine()) != null){
                lineTxt = lineTxt.trim();
                addKeyword(lineTxt);

            }
            inputStreamReader.close();
        }catch (Exception e){
            logger.error("读取敏感词文件失败" + e.getMessage());
        }

    }

    private class TrieNode{

        private boolean end = false;

        private Map<Character,TrieNode> subNodes = new HashMap<Character,TrieNode>();

        void addSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }

        TrieNode getSubNode(Character key){
           return subNodes.get(key);
        }

        boolean isKeywordEnd(){
            return end;
        }

        void setKeywordEnd(boolean end){
            this.end = end;
        }

        public int getSubNodeSize(){
            return subNodes.size();
        }
    }



    private TrieNode rootNode = new TrieNode(); // 创建一个根节点

    public String doFilter(String text){
        String sensitiveStr = DEFAULT_REPLACEMENT;
        StringBuilder result = new StringBuilder();
//        StringBuffer stringBuffer = new StringBuffer();
        if (StringUtils.isBlank(text)){
            return result.toString();
        }
        TrieNode tempNode = rootNode; // 创建一个临时指针指向根节点；
        int begin = 0; // 回滚数
        int position = 0; // 当前比较的位置

        while (position < text.length()){
            // 开始比较
            char c = text.charAt(position); // 当前比较的字符

            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }
            tempNode = tempNode.getSubNode(c); // 前缀树的当前位置的值
            if(tempNode == null){
                // 在敏感词树里面没有找到，说明c字符不是敏感词
                result.append(text.charAt(begin));
                position = begin + 1;// 后移
                begin = position; // 后移
                tempNode = rootNode; // 再次指向根节点
            }else if (tempNode.isKeywordEnd()){
                // 表示此时是敏感词的最后一位的
                result.append(sensitiveStr);// 替换text中的敏感词
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            }else {
                ++position;
            }


        }
        result.append(text.substring(begin));
        return result.toString();

    }

    public void addKeyword(String lineTxt){

        TrieNode tempNode = rootNode;
        for (int i = 0; i < lineTxt.length(); i++){
            Character c = lineTxt.charAt(i);
            // 过滤空格
            if (isSymbol(c)) {
                continue;
            }
            TrieNode node = tempNode.getSubNode(c);
            if(node == null){
                node = new TrieNode();
                tempNode.addSubNode(c,node);
            }
            tempNode = node;
            if (i == lineTxt.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }




//    public static void main(String[] args) {
//
//        SensitiveService s = new SensitiveService();
//        s.addKeyword("abc");
//        s.addKeyword("ef");
//        System.out.println("123abcc122:"+s.doFilter("123abcc122"));
//    }

}
