package com.xinys.wenda.service;

import com.xinys.wenda.dao.CommentDAO;
import com.xinys.wenda.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentDAO commentDAO;


    /**
     * 查找词问题的所有评论
     * @param entityId 问题id
     * @param entityType 问题
     * @return
     */
    public List<Comment> getCommentsByEntity(int entityId, int entityType) {
        return commentDAO.selectByEntity(entityId, entityType);
    }

    /**
     * 添加评论
     * @param comment
     * @return
     */
    public int addComment(Comment comment) {
        return commentDAO.addComment(comment);
    }

    /**
     * 评论数量
     * @param entityId
     * @param entityType
     * @return
     */
    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }


    /**
     * 删除某一个评论
     * @param entityId
     * @param entityType
     */
    public void deleteComment(int entityId, int entityType) {
        commentDAO.updateStatus(entityId, entityType, 1);
    }


    public Comment getCommentById(int id){
        return commentDAO.getCOmmentById(id);
    }


    public int getUserCommentCount(int userId){
        return commentDAO.getUserCommentCount(userId);
    }
}
