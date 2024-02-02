package com.example.tiktokelpuig;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments = new ArrayList<>();

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView authorTextViewComment;
        private final TextView commentEditText;
        private Button postCommentButton;
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextViewComment = itemView.findViewById(R.id.authorTextViewComment);
            commentEditText = itemView.findViewById(R.id.commentEditText);
            postCommentButton = itemView.findViewById(R.id.postCommentButton);
        }

        void bind(Comment comment) {
            authorTextViewComment.setText(comment.getAuthor());
            commentEditText.setText(comment.getContent());
        }
    }
}



