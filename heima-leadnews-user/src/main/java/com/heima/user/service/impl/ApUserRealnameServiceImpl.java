package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constans.user.UserConstants;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.feign.ArticleFeign;
import com.heima.user.feign.WemediaFeign;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {
    //@Transactional
    @Transactional
    public void test(){
        System.out.println("123");
    }
    @Override
    public ResponseResult loadListByStatus(AuthDto dto) {
        //1.检查参数
        if(dto == null ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页检查
        dto.checkParam();
        //2.根据状态分页查询
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(dto.getStatus() != null){
            lambdaQueryWrapper.eq(ApUserRealname::getStatus,dto.getStatus());
        }
        //分页条件构建
        IPage pageParam = new Page(dto.getPage(),dto.getSize());
        IPage page = page(pageParam, lambdaQueryWrapper);
        //3.返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }
    @Override
    @GlobalTransactional
    public ResponseResult updateStatusById(AuthDto dto, Short status) {
        ResponseResult res = null;
        boolean finished = false;
        //1.检查参数
        if (dto == null || dto.getId() == null) {
            res = ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        } else if (checkStatus(status)) {
            res = ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        } else {//2.修改状态
            ApUserRealname apUserRealname = new ApUserRealname();
            apUserRealname.setId(dto.getId());
            apUserRealname.setStatus(status);
            if (dto.getMsg() != null) {
                apUserRealname.setReason(dto.getMsg());
            }
            updateById(apUserRealname);//3.如果审核状态是通过，创建自媒体账户，创建作者信息
            if (status.equals(UserConstants.PASS_AUTH)) {
                //创建自媒体账户，创建作者信息
                ResponseResult result = createWmUserAndAuthor(dto);
             /*   if (result != null) {
                  *//*  res = result;
                    finished = true;*//*
                    return result;
                }
            }
           *//* if (!finished) {
//                int a = 1 / 0;
                res = ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
            }*//*
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);*/

                if (result != null) {
                    res = result;
                    finished = true;
                }
            }
            if (!finished) {
                int a = 1 / 0;
                res = ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
            }
        }

        return res;
    }
    @Autowired
    private ApUserMapper apUserMapper;
    @Autowired
    private WemediaFeign wemediaFeign;
    /**
     *  创建自媒体账户，创建作者信息
     * @param dto
     */
    private ResponseResult createWmUserAndAuthor(AuthDto dto) {
        //获取ap_user信息
        Integer apUserRealnameId = dto.getId();
        ApUserRealname apUserRealname = getById(apUserRealnameId);
        ApUser apUser = apUserMapper.selectById(apUserRealname.getUserId());
        if(apUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //远程调用feign接口，判断用户名是否存在
        WmUser wmUser = wemediaFeign.findByName(apUser.getName());
        //创建自媒体账户
        if(wmUser == null){
            wmUser = new WmUser();
            wmUser.setApUserId(apUser.getId());
            wmUser.setCreatedTime(new Date());
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setPhone(apUser.getPhone());
            wmUser.setStatus(9);
            ResponseResult save = wemediaFeign.save(wmUser);
//            if(save.getCode() != 0){
//                throw new RuntimeException("调用远程接口失败");
//            }
        }
        //创建作者
        createAuthor(wmUser);
        apUser.setFlag((short)1);
        apUserMapper.updateById(apUser);
        return  null;
    }
    @Autowired
    private ArticleFeign articleFeign;
    /**
     * 创建作者
     * @param wmUser
     */
    private void createAuthor(WmUser wmUser) {
        Integer apUserId = wmUser.getApUserId();
        ApAuthor apAuthor = articleFeign.findByUserId(apUserId);
        if(apAuthor == null){
            apAuthor = new ApAuthor();
            apAuthor.setName(wmUser.getName());
            apAuthor.setCreatedTime(new Date());
            apAuthor.setUserId(apUserId);
            apAuthor.setType(UserConstants.AUTH_TYPE);
            articleFeign.save(apAuthor);
        }
    }

    /**
     * 检查状态
     * @param status
     * @return
     */
    private boolean checkStatus(Short status) {
        if(status == null || (!status.equals(UserConstants.FAIL_AUTH) && !status.equals(UserConstants.PASS_AUTH))){
            return  true;
        }
        return false;
    }
}
