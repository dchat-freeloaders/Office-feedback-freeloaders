package com.github.iauglov.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedBackRepository extends JpaRepository<FeedBack, Integer> {

    List<FeedBack> findAllByEventId(int eventId);

}
