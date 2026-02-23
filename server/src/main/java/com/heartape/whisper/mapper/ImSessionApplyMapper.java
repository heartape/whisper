package com.heartape.whisper.mapper;

import com.heartape.whisper.common.constant.ApplyBizTypeEnum;
import com.heartape.whisper.common.constant.ApplyStatusEnum;
import com.heartape.whisper.entity.ImSessionApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImSessionApplyMapper {

    int insert(ImSessionApply imSessionApply);

    ImSessionApply selectById(Long id);

    int updateReview(@Param("id") Long id,
                     @Param("status") ApplyStatusEnum status,
                     @Param("reviewerId") Long reviewerId,
                     @Param("reviewNote") String reviewNote);

    ImSessionApply selectByApplicantIdAndReviewerId(Long applicantId, Long reviewerId, ApplyBizTypeEnum bizType);

    ImSessionApply pendingGroupApply(Long sessionId, Long applicantId);

    List<ImSessionApply> selectListByReviewerId(Long reviewerId);

    List<ImSessionApply> selectListByApplicantId(Long applicantId);

    int updateForReset(ImSessionApply apply);
}
