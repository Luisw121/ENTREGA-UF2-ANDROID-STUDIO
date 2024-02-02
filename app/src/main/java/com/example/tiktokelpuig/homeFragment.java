package com.example.tiktokelpuig;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class homeFragment extends Fragment {

    NavController navController;
    public AppViewModel appViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").limit(50);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
        new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
            super(options);
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Crear el ViewHolder con el diseño viewholder_post.xml
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false);

            return new PostViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position,
                                        @NonNull final Post post) {
            // Gestion de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);
            if (post.likes.containsKey(uid))
                holder.likeImageView.setImageResource(R.drawable.like_on);
            else
                holder.likeImageView.setImageResource(R.drawable.like_off);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes." + uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
            });
            //Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {

                holder.mediaImageView.setVisibility(View.GONE);
            }
            //BOTON DE ELIMINAR
            holder.deleteButton.setOnClickListener(view ->{
                //obtenemos la posición del elemento en el adptador
                int adaptadorPosition = holder.getAdapterPosition();

                //Nos aseguramos de que la posición sea válida
                if (adaptadorPosition != RecyclerView.NO_POSITION) {
                    //obtenet el ID del documento a eliminar
                    String documentId = getSnapshots().getSnapshot(adaptadorPosition).getId();

                    //Eliminamos el documento de Firestore
                    FirebaseFirestore.getInstance().collection("posts")
                            .document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                //Exito al eliminar
                            })
                            .addOnFailureListener(e -> {
                                //Manejar fallo en la eliminación
                            });
                }
            });
            //Para los comentarios
            holder.bindComments(post.getComments());
            List<Comment> comments = post.getComments();
            if (comments != null && !comments.isEmpty()) {
                holder.commentsRecyclerView.setVisibility(View.VISIBLE);
                CommentsAdapter commentsAdapter = new CommentsAdapter();
                commentsAdapter.setComments(comments);
                holder.commentsRecyclerView.setAdapter(commentsAdapter);
            }else {
                holder.commentsRecyclerView.setVisibility(View.GONE);
            }
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView;
            TextView authorTextView, contentTextView, numLikesTextView;
            ImageButton deleteButton;
            RecyclerView commentsRecyclerView;
            CommentsAdapter commentsAdapter; //

            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                commentsRecyclerView = itemView.findViewById(R.id.commentsRecyclerView);
                commentsAdapter = new CommentsAdapter();
                commentsRecyclerView.setAdapter(commentsAdapter);
            }
            void bindComments(List<Comment> comments) {
                commentsAdapter.setComments(comments);

                if (comments != null && !comments.isEmpty()) {
                    commentsRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    commentsRecyclerView.setVisibility(View.GONE);
                }
            }
        }
    }

}
