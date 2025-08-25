package com.moimlog.moimlog_backend.service;

import com.moimlog.moimlog_backend.dto.request.CreateScheduleRequest;
import com.moimlog.moimlog_backend.entity.*;
import com.moimlog.moimlog_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 일정 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository scheduleParticipantRepository;
    private final MoimRepository moimRepository;
    private final UserRepository userRepository;
    
    /**
     * 일정 목록 조회
     */
    public Map<String, Object> getSchedules(Long moimId, String startDate, String endDate, String type) {
        List<Schedule> schedules;
        
        if (startDate != null && endDate != null) {
            // 날짜 범위로 조회
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            
            if (type != null && !type.equals("all")) {
                Schedule.ScheduleType scheduleType = Schedule.ScheduleType.valueOf(type.toUpperCase());
                schedules = scheduleRepository.findByMoimIdAndTypeAndDateRange(moimId, scheduleType, start, end);
            } else {
                schedules = scheduleRepository.findByMoimIdAndDateRange(moimId, start, end);
            }
        } else {
            // 전체 일정 조회
            if (type != null && !type.equals("all")) {
                Schedule.ScheduleType scheduleType = Schedule.ScheduleType.valueOf(type.toUpperCase());
                Page<Schedule> schedulePage = scheduleRepository.findByMoimIdAndTypeOrderByStartDateAsc(moimId, scheduleType, Pageable.unpaged());
                schedules = schedulePage.getContent();
            } else {
                Page<Schedule> schedulePage = scheduleRepository.findByMoimIdOrderByStartDateAsc(moimId, Pageable.unpaged());
                schedules = schedulePage.getContent();
            }
        }
        
        // DTO 변환
        List<Map<String, Object>> scheduleResponses = schedules.stream()
                .map(this::convertToScheduleResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("schedules", scheduleResponses);
        
        return response;
    }
    
    /**
     * 일정 등록
     */
    public Schedule createSchedule(Long moimId, Long userId, CreateScheduleRequest request) {
        // 모임 존재 확인
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 일정 생성
        Schedule schedule = Schedule.builder()
                .moim(moim)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .locationDetail(request.getLocationDetail())
                .maxAttendees(request.getMaxAttendees())
                .type(request.getType())
                .isRecurring(request.getIsRecurring())
                .recurrenceRule(request.getRecurrenceRule())
                .createdBy(user)
                .build();
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // 작성자를 자동으로 참석자로 추가
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(savedSchedule)
                .user(user)
                .status(ScheduleParticipant.Status.ATTENDING)
                .build();
        scheduleParticipantRepository.save(participant);
        
        return savedSchedule;
    }
    
    /**
     * 일정 참석 처리
     */
    public Map<String, Object> participateSchedule(Long scheduleId, Long userId, ScheduleParticipant.Status status) {
        // 일정 존재 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이미 참가자로 등록되어 있는지 확인
        ScheduleParticipant existingParticipant = scheduleParticipantRepository
                .findByScheduleIdAndUserId(scheduleId, userId)
                .orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (existingParticipant != null) {
            // 기존 참가자 상태 업데이트
            existingParticipant.setStatus(status);
            scheduleParticipantRepository.save(existingParticipant);
            
            response.put("action", "updated");
            response.put("message", "참석 상태가 업데이트되었습니다.");
        } else {
            // 새로운 참가자 추가
            ScheduleParticipant newParticipant = ScheduleParticipant.builder()
                    .schedule(schedule)
                    .user(user)
                    .status(status)
                    .build();
            scheduleParticipantRepository.save(newParticipant);
            
            response.put("action", "added");
            response.put("message", "일정에 참가자로 등록되었습니다.");
        }
        
        response.put("scheduleId", scheduleId);
        response.put("userId", userId);
        response.put("status", status);
        response.put("userName", user.getName());
        
        return response;
    }
    
    /**
     * 일정을 응답 DTO로 변환
     */
    private Map<String, Object> convertToScheduleResponse(Schedule schedule) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", schedule.getId());
        response.put("title", schedule.getTitle());
        response.put("description", schedule.getDescription());
        response.put("startDate", schedule.getStartDate());
        response.put("endDate", schedule.getEndDate());
        response.put("location", schedule.getLocation());
        response.put("locationDetail", schedule.getLocationDetail());
        response.put("maxAttendees", schedule.getMaxAttendees());
        response.put("type", schedule.getType());
        response.put("isRecurring", schedule.getIsRecurring());
        response.put("recurrenceRule", schedule.getRecurrenceRule());
        response.put("createdBy", schedule.getCreatedBy().getId());
        response.put("creatorName", schedule.getCreatedBy().getName());
        response.put("createdAt", schedule.getCreatedAt());
        
        // 참가자 정보 추가
        List<Map<String, Object>> participants = schedule.getParticipants().stream()
                .map(this::convertToParticipantResponse)
                .collect(Collectors.toList());
        response.put("participants", participants);
        
        return response;
    }
    
    /**
     * 참가자를 응답 DTO로 변환
     */
    private Map<String, Object> convertToParticipantResponse(ScheduleParticipant participant) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", participant.getUser().getId());
        response.put("userName", participant.getUser().getName());
        response.put("status", participant.getStatus());
        return response;
    }
}
