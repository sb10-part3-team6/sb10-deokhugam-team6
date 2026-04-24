package com.codeit.mission.deokhugam.dashboard.popularbooks.repository;

import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

}
