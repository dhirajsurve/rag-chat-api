package com.rag.chat.api.rag.chat.api.repo;

import com.rag.chat.api.rag.chat.api.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    Optional<UserFile> findFirstByUserIdAndFileNameOrderByCreatedDateDesc(String userId, String fileName);

    @Query(value = " SELECT distinct file_name FROM public.user_files WHERE user_id= :param1"
            , nativeQuery = true)
    List<String> findDistinctFileNameByUserId(@Param("param1")String userId);
}