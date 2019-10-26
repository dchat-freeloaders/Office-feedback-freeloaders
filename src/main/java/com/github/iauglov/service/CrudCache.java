package com.github.iauglov.service;

import com.github.iauglov.persistence.FeedBack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CrudCache {

    public final Map<Integer, Integer> eventEditingMap = new HashMap<>();
    public final Set<Integer> eventCreatingMap = new HashSet<>();

    public final Map<Integer, FeedBack> feedBackMap = new HashMap<>();

}
