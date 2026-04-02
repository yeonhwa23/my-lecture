package com.sp.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.member.Report;

/**
 * reports 테이블
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    /** 처리 상태별 신고 목록 (관리자 페이지) */
    Page<Report> findByReportStatusOrderByReportDateDesc(Integer reportStatus, Pageable pageable);

    /** 동일 신고 중복 체크 (target + targetNum + reporter) */
    boolean existsByTargetAndTargetNumAndReporter_MemberId(
            String target, Long targetNum, Long reporterId);
}
