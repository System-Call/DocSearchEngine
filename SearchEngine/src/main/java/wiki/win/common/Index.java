package wiki.win.common;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import wiki.win.pojo.DocInfo;
import wiki.win.pojo.Weight;

import java.util.*;

/**
 * @program: JavaApiSearchEngine
 * @description: 文档索引
 * @author: xiedy
 * @create: 2023-02-06 10:56
 **/
public class Index {


    /**
     * 文档列表
     */
    private List<DocInfo> forwardIndex = new ArrayList<>();
    /**
     * 词和文档的 关联度
     */
    private Map<String, ArrayList<Weight>> invertedIndex = new HashMap<>();


    /**
     * 给定一个docId ,在正排索引中，查询文档的详细信息
     *
     * @param docId
     * @return
     */
    public DocInfo getDocInfo(int docId) {
        return forwardIndex.get(docId);
    }

    /**
     * 给定一词，在倒排索引中，查哪些文档和这个文档词关联
     *
     * @param term
     * @return
     */

    private List<Weight> getInverted(String term) {
        return invertedIndex.get(term);
    }

    /**
     * 往索引中新增一个文档 同时构建他的正排和倒排索引
     *
     * @param title
     * @param url
     * @param content
     */

    public void addDoc(String title, String url, String content) {
        //构建正排索引 并将文档添加到文档列表
        DocInfo docInfo = buildForward(title, url, content);

    }

    /**
     * 把内存中的索引结构保存到磁盘中
     */
    public void save() {
        //TODO

    }

    /**
     * 把磁盘中的索引数据加载到内存中
     */
    public void load() {
        //TODO

    }

    /**
     * 正排 索引值为列表下标,并封装到对应docInfo
     *
     * @param title
     * @param url
     * @param content
     * @return DocInfo
     */
    public DocInfo buildForward(String title, String url, String content) {
        DocInfo docInfo = new DocInfo();
        //封装
        docInfo.setDocId(forwardIndex.size());
        docInfo.setContent(content);
        docInfo.setTitle(title);
        docInfo.setUrl(url);
        //添加到文档列表
        forwardIndex.add(docInfo);
        return docInfo;
    }

    private void buildInverted(DocInfo docInfo) {

        class WordCount {
            //
            public int titleCount;
            //
            public int contentCount;
        }

        /**
         * 统计词频
         */
        HashMap<String, WordCount> wordCountMap = new HashMap<>();

        //标题分词
        List<Term> termTitle = ToAnalysis.parse(docInfo.getTitle()).getTerms();
        //遍历 统计
        termTitle.forEach(t -> {
            //
            String word = t.getName();
            //
            WordCount wordCount = wordCountMap.get(word);
            if (wordCount == null) {
                WordCount wc = new WordCount();
                wc.titleCount = 1;
                wc.contentCount = 0;
                wordCountMap.put(word, wc);
            } else {
                wordCount.titleCount++;
            }
        });
        //正文分词
        List<Term> termsContent = ToAnalysis.parse(docInfo.getContent()).getTerms();
        //
        termsContent.forEach(t -> {
            //
            String word = t.getName();
            //
            WordCount wordCount = wordCountMap.get(word);
            if (wordCount == null) {
                WordCount wc = new WordCount();
                wc.titleCount = 1;
                wc.contentCount = 0;
                wordCountMap.put(word, wc);
            } else {
                wordCount.titleCount++;
            }
        });

        //根据词频统计 构建 词和文档的相关度 公式 : 词和文档的相关度 = 词在文档标题的频率*10 + 词在文档中频率
        //wordCountMap由Map转为Set后可以遍历
        Set<Map.Entry<String, WordCount>> entrySet = wordCountMap.entrySet();
        entrySet.forEach(e -> {
            String key = e.getKey();
            ArrayList<Weight> weightList = invertedIndex.get(key);

            if (weightList == null) {
                //如果不存在则新建键值对 ,并插入相关度列表
                weightList = new ArrayList<>();
                Weight weight = new Weight();
                weight.setDocId(docInfo.getDocId());
                //词和文档的相关度
                weight.setWeight(e.getValue().titleCount * 10 + e.getValue().contentCount);
                weightList.add(weight);
                invertedIndex.put(key,weightList);
            }else {
                //如果存在则在相关度列表后新增新的相关度
                Weight weight = new Weight();
                weight.setDocId(docInfo.getDocId());
                //词和文档的相关度
                weight.setWeight(e.getValue().titleCount * 10 + e.getValue().contentCount);
                weightList.add(weight);
            }
        });

    }
}
