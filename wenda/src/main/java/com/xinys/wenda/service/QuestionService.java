package com.xinys.wenda.service;


import com.xinys.wenda.dao.QuestionDAO;
import com.xinys.wenda.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 问题的业务实现类
 */
@Service
public class QuestionService {
    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    SensitiveService sensitiveService;

    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    /**
     * 添加问题的业务逻辑
     * @param question
     * @return
     */
    public int addQuestion(Question question){
        // 过滤掉HTML js 脚本语言
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));

        // 过滤掉敏感词或者广告
        question.setTitle(sensitiveService.doFilter(question.getTitle()));
        question.setContent(sensitiveService.doFilter(question.getContent()));
        return questionDAO.addQuestion(question) > 0 ? 1:0;
    }


    public Question selectById(int qid){
        return questionDAO.selectById(qid);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }
}
