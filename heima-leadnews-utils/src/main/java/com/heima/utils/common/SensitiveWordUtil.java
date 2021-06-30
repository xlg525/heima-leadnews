package com.heima.utils.common;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SensitiveWordUtil {

    public static Map<String, Object> dictionaryMap = new HashMap<>();
    //读的时候，如果加读锁，不影响其他读锁写的时候，加了写锁，其他的读锁和写锁都不能获取到锁
    public static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    //CAS compare and set 当前数据是否初始化
    public static AtomicBoolean isInit = new AtomicBoolean(false);

    public static boolean needToInit(){
        return !isInit.get();
    }
    /**
     * 生成关键词字典库
     * @param words
     * @return
     */
    public static void initMap(Collection<String> words) {

        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        try{
            writeLock.lock();

            if(isInit.get()){
                //已经被创建,直接返回
                return;
            }

            if (words == null) {
                System.out.println("敏感词列表不能为空");
                return ;
            }

            // map初始长度words.size()，整个字典库的入口字数(小于words.size()，因为不同的词可能会有相同的首字)
            Map<String, Object> map = new HashMap<>(words.size());
            // 遍历过程中当前层次的数据
            Map<String, Object> curMap = null;
            Iterator<String> iterator = words.iterator();

            while (iterator.hasNext()) {
                String word = iterator.next();
                curMap = map;
                int len = word.length();
                for (int i =0; i < len; i++) {
                    // 遍历每个词的字
                    String key = String.valueOf(word.charAt(i));
                    // 当前字在当前层是否存在, 不存在则新建, 当前层数据指向下一个节点, 继续判断是否存在数据
                    Map<String, Object> wordMap = (Map<String, Object>) curMap.get(key);
                    if (wordMap == null) {
                        // 每个节点存在两个数据: 下一个节点和isEnd(是否结束标志)
                        wordMap = new HashMap<>(2);
                        wordMap.put("isEnd", "0");
                        curMap.put(key, wordMap);
                    }
                    curMap = wordMap;
                    // 如果当前字是词的最后一个字，则将isEnd标志置1
                    if (i == len -1) {
                        curMap.put("isEnd", "1");
                    }
                }
            }

            dictionaryMap = map;
            //CAS
            isInit.compareAndSet(false,true);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

    }

    /**
     * 搜索文本中某个文字是否匹配关键词
     * @param text
     * @param beginIndex
     * @return
     */
    private static int checkWord(String text, int beginIndex) {
        if (dictionaryMap == null) {
            throw new RuntimeException("字典不能为空");
        }
        boolean isEnd = false;
        int wordLength = 0;
        Map<String, Object> curMap = dictionaryMap;
        int len = text.length();
        // 从文本的第beginIndex开始匹配
        for (int i = beginIndex; i < len; i++) {
            String key = String.valueOf(text.charAt(i));
            // 获取当前key的下一个节点
            curMap = (Map<String, Object>) curMap.get(key);
            if (curMap == null) {
                break;
            } else {
                wordLength ++;
                if ("1".equals(curMap.get("isEnd"))) {
                    isEnd = true;
                }
            }
        }
        if (!isEnd) {
            wordLength = 0;
        }
        return wordLength;
    }

    /**
     * 获取匹配的关键词和命中次数
     * @param text
     * @return
     */
    public static Map<String, Integer> matchWords(String text) {

        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        Map<String, Integer> wordMap = new HashMap<>();
        try{
            readLock.lock();
            int len = text.length();
            for (int i = 0; i < len; i++) {
                int wordLength = checkWord(text, i);
                if (wordLength > 0) {
                    String word = text.substring(i, i + wordLength);
                    // 添加关键词匹配次数
                    if (wordMap.containsKey(word)) {
                        wordMap.put(word, wordMap.get(word) + 1);
                    } else {
                        wordMap.put(word, 1);
                    }

                    i += wordLength - 1;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }

        return wordMap;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("法轮");
        list.add("法轮功");
        list.add("冰毒");
        initMap(list); //将list<String>敏感词->树 map
        String content="我是一个好人，并不会卖冰毒，也不操练法轮功,我真的不卖冰毒";
        Map<String, Integer> map = matchWords(content);
        boolean empty = map.isEmpty();
        System.out.println(map);
    }
}
