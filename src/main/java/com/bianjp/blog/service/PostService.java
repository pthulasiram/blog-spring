package com.bianjp.blog.service;

import com.bianjp.blog.entity.Post;
import com.bianjp.blog.entity.Tag;
import com.bianjp.blog.form.PostForm;
import com.bianjp.blog.repository.PostRepository;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
  private static List<Post.Status> normalStatuses =
      Arrays.asList(Post.Status.PUBLISHED, Post.Status.UNPUBLISHED);

  private final PostRepository postRepository;
  private final TagService tagService;

  @Autowired
  public PostService(PostRepository postRepository, TagService tagService) {
    this.postRepository = postRepository;
    this.tagService = tagService;
  }

  public List<Post> findDrafts(Pageable pageable) {
    return postRepository.findAllByStatus(Post.Status.DRAFT, pageable);
  }

  public int countDrafts() {
    return postRepository.countAllByStatus(Post.Status.DRAFT);
  }

  public List<Post> findNormalPosts(Pageable pageable) {
    return postRepository.findAllByStatusIn(normalStatuses, pageable);
  }

  public int countNormalPosts() {
    return postRepository.countAllByStatusIn(normalStatuses);
  }

  public Post findPublishedPost(LocalDate publishDate, String slug) {
    return postRepository.findByPublishDateAndSlugAndStatus(
        publishDate, slug, Post.Status.PUBLISHED);
  }

  public Post findPublishedPost(int id) {
    return postRepository.findByIdAndStatus(id, Post.Status.PUBLISHED);
  }

  public List<Post> findLatestPublishedPosts(int count) {
    Sort sort = new Sort(Sort.Direction.DESC, "id");
    PageRequest pageRequest = new PageRequest(0, count, sort);
    return postRepository.findAllByStatus(Post.Status.PUBLISHED, pageRequest);
  }

  public Post create(PostForm postForm) {
    Post post = new Post();
    post.setTitle(postForm.getTitle());
    post.setSlug(postForm.getSlug());
    post.setContent(postForm.getContent());
    post.setStatus(postForm.getStatus());

    updateTags(post, postForm.getTags());

    if (post.isPublished()) {
      post.initPublishDate();
    }

    postRepository.save(post);
    return post;
  }

  public void update(Post post, PostForm postForm) {
    post.setTitle(postForm.getTitle());
    post.setSlug(postForm.getSlug());
    post.setContent(postForm.getContent());
    post.setStatus(postForm.getStatus());

    updateTags(post, postForm.getTags());

    if (post.isPublished()) {
      post.initPublishDate();
    }

    postRepository.save(post);
  }

  public void logicalDelete(Post post) {
    post.setStatus(Post.Status.DELETED);
    postRepository.save(post);
  }

  private void updateTags(Post post, List<String> tagNames) {
    Set<Tag> newTags, oldTags = post.getTags();

    if (tagNames == null || tagNames.isEmpty()) {
      newTags = new HashSet<>();
    } else {
      newTags = tagNames.stream().map(tagService::findOrCreateByName).collect(Collectors.toSet());
    }

    Set<Tag> removedTags = new HashSet<>(oldTags);
    removedTags.removeAll(newTags);

    Set<Tag> addedTags = new HashSet<>(newTags);
    addedTags.removeAll(oldTags);

    // Tags not changed
    if (removedTags.isEmpty() && addedTags.isEmpty()) {
      return;
    }

    for (Tag tag : removedTags) {
      post.removeTag(tag);
    }

    for (Tag tag : addedTags) {
      post.addTag(tag);
    }
  }
}
