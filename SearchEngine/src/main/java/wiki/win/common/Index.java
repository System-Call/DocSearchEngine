package wiki.win.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import wiki.win.pojo.DocInfo;
import wiki.win.pojo.Weight;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @program: JavaApiSearchEngine
 * @description: 文档索引
 * @author: xiedy
 * @create: 2023-02-06 10:56
 **/
public class Index {

    private static File rootPath;

    //初始化
    static {
        try {
            rootPath = new File("").getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String INDEX_PATH = rootPath + "\\doc_search_index\\";

    /**
     * 序列化类
     */
    ObjectMapper objectMapper = new ObjectMapper();

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
        //构建倒排索引
        buildInverted(docInfo);
    }

    /**
     * 把内存中的索引结构保存到磁盘中
     */
    public void save() {
        //TODO

        File indexPathFile = new File(INDEX_PATH);
        if(!indexPathFile.exists()){
            indexPathFile.mkdirs();
        }
        File forwardFile = new File(INDEX_PATH + "forward.txt");
        File invertedFile = new File(INDEX_PATH + "inverted.txt");
        try {
            objectMapper.writeValue(forwardFile,forwardIndex);
            objectMapper.writeValue(invertedFile,invertedIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 把磁盘中的索引数据加载到内存中
     */
    public void load() {
        //TODO
        //1.设置加载索引路径
        //正排索引
        File forwardIndexFile = new File(INDEX_PATH+"forward.txt");
        //倒排索引
        File invertedIndexFile = new File(INDEX_PATH+"inverted.txt");
        try{
            //readValue（）2个参数，从那个文件读，解析是什么数据
            forwardIndex = objectMapper.readValue(forwardIndexFile, new TypeReference<ArrayList<DocInfo>>() {});
            invertedIndex = objectMapper.readValue(invertedIndexFile, new TypeReference<HashMap<String, ArrayList<Weight>>>() {});
        }catch (IOException e){
            e.printStackTrace();
        }
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
        synchronized (forwardIndex){
            docInfo.setDocId(forwardIndex.size());
            forwardIndex.add(docInfo);
        }
        docInfo.setContent(content);
        docInfo.setTitle(title);
        docInfo.setUrl(url);
        //添加到文档列表
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
        synchronized (invertedIndex){
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
                    invertedIndex.put(key, weightList);
                } else {
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
}
