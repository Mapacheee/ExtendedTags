package me.mapacheee.extendedtags.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedtags.data.Tag;
import me.mapacheee.extendedtags.data.TagStorage;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Comparator;

@Service
public final class TagService {

    private final TagStorage tagStorage;
    private final Logger logger;

    @Inject
    public TagService(TagStorage tagStorage, Logger logger) {
        this.tagStorage = tagStorage;
        this.logger = logger;
    }

    public Collection<Tag> getAllTags() {
        return tagStorage.getAllTags().stream()
                .sorted(Comparator.comparingInt(Tag::getPriority)
                .reversed()
                .thenComparing(Tag::getKey))
                .toList();
    }

    public Tag getTag(String key) {
        return tagStorage.getTag(key);
    }

    public void saveTag(Tag tag) {
        tagStorage.saveTag(tag);
        logger.info("Saved tag: {}", tag.getKey());
    }

    public boolean deleteTag(String key) {
        Tag tag = tagStorage.getTag(key);
        if (tag == null) {
            return false;
        }
        tagStorage.deleteTag(key);
        logger.info("Deleted tag: {}", key);
        return true;
    }

    public void reload() {
        tagStorage.reload();
        logger.info("Reloaded {} tags", tagStorage.getAllTags().size());
    }
}