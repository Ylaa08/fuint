package com.fuint.module.clientApi.controller;

import com.fuint.common.dto.AddressDto;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.AddressService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtRegionMapper;
import com.fuint.repository.model.MtAddress;
import com.fuint.repository.model.MtRegion;
import com.fuint.utils.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.InvocationTargetException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收货地址controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@RestController
@RequestMapping(value = "/clientApi/address")
public class ClientAddressController extends BaseController {

    /**
     * 收货地址服务接口
     * */
    @Autowired
    private AddressService addressService;

    @Resource
    private MtRegionMapper mtRegionMapper;

    /**
     * 保存收货地址
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject save(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String name = param.get("name") == null ? "" : param.get("name").toString();
        String mobile = param.get("mobile") == null ? "" : param.get("mobile").toString();
        Integer provinceId = param.get("provinceId") == null ? 0 : Integer.parseInt(param.get("provinceId").toString());
        Integer cityId = param.get("cityId") == null ? 0 : Integer.parseInt(param.get("cityId").toString());
        Integer regionId = param.get("regionId") == null ? 0 : Integer.parseInt(param.get("regionId").toString());
        String detail = param.get("detail") == null ? "" : param.get("detail").toString();
        String status = param.get("status") == null ? "" : param.get("status").toString();
        String isDefault = param.get("isDefault") == null ? "" : param.get("isDefault").toString();
        Integer addressId = param.get("addressId") == null ? 0 : Integer.parseInt(param.get("addressId").toString());

        if (StringUtil.isEmpty(token)) {
            return getFailureResult(1001);
        }

        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);
        if (null == mtUser) {
            return getFailureResult(1001);
        }

        MtAddress mtAddress = new MtAddress();
        mtAddress.setId(addressId);
        mtAddress.setName(name);
        mtAddress.setMobile(mobile);
        mtAddress.setProvinceId(provinceId);
        mtAddress.setCityId(cityId);
        mtAddress.setRegionId(regionId);
        mtAddress.setDetail(detail);
        mtAddress.setStatus(status);
        mtAddress.setUserId(mtUser.getId());
        mtAddress.setIsDefault(isDefault);

        addressService.saveAddress(mtAddress);

        return getSuccessResult(true);
    }

    /**
     * 获取收货地址列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException, InvocationTargetException, IllegalAccessException {
        String token = request.getHeader("Access-Token");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> param = new HashMap<>();

        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);
        if (null == mtUser) {
            return getFailureResult(1001);
        } else {
            param.put("userId", mtUser.getId());
        }

        param.put("status", StatusEnum.ENABLED.getKey());

        List<MtAddress> addressList = addressService.queryListByParams(param);

        List<AddressDto> dataList = new ArrayList<>();
        for (MtAddress mtAddress : addressList) {
            AddressDto dto = new AddressDto();
            BeanUtils.copyProperties(mtAddress, dto);

            String province = "";
            String city = "";
            String region = "";

            if (dto.getProvinceId() > 0) {
                MtRegion mtProvince = mtRegionMapper.selectById(dto.getProvinceId());
                if (mtProvince != null) {
                    province = mtProvince.getName();
                }
            }
            if (dto.getCityId() > 0) {
                MtRegion mtCity = mtRegionMapper.selectById(dto.getCityId());
                if (mtCity != null) {
                    city = mtCity.getName();
                }
            }
            if (dto.getCityId() > 0) {
                MtRegion mtRegion = mtRegionMapper.selectById(dto.getRegionId());
                if (mtRegion != null) {
                    region = mtRegion.getName();
                }
            }

            dto.setProvinceName(province);
            dto.setCityName(city);
            dto.setRegionName(region);

            dataList.add(dto);
        }

        result.put("list", dataList);

        return getSuccessResult(result);
    }

    /**
     * 获取收货地址详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject detail(HttpServletRequest request) throws BusinessCheckException, InvocationTargetException, IllegalAccessException {
        String addressIdStr = request.getParameter("addressId") == null ? "" : request.getParameter("addressId");
        Integer addressId = 0;
        if (StringUtil.isNotEmpty(addressIdStr)) {
            addressId = Integer.parseInt(addressIdStr);
        }

        String token = request.getHeader("Access-Token");

        Map<String, Object> result = new HashMap<>();

        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);

        if (null == mtUser || StringUtil.isEmpty(token)) {
            return getFailureResult(1001);
        }

        MtAddress mtAddress = null;
        if (addressId > 0) {
            mtAddress = addressService.detail(addressId);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", mtUser.getId());
            params.put("isDefault", YesOrNoEnum.YES.getKey());
            List<MtAddress> addressList = addressService.queryListByParams(params);
            if (addressList.size() > 0) {
                mtAddress = addressList.get(0);
            }
        }

        if (mtAddress != null) {
            if (mtAddress.getUserId().equals(mtUser.getId())) {
                AddressDto dto = new AddressDto();
                BeanUtils.copyProperties(mtAddress, dto);
                String province = "";
                String city = "";
                String region = "";
                if (dto.getProvinceId() > 0) {
                    MtRegion mtProvince = mtRegionMapper.selectById(dto.getProvinceId());
                    if (mtProvince != null) {
                        province = mtProvince.getName();
                    }
                }
                if (dto.getCityId() > 0) {
                    MtRegion mtCity = mtRegionMapper.selectById(dto.getCityId());
                    if (mtCity != null) {
                        city = mtCity.getName();
                    }
                }
                if (dto.getRegionId() > 0) {
                    MtRegion mtRegion = mtRegionMapper.selectById(dto.getRegionId());
                    if (mtRegion != null) {
                        region = mtRegion.getName();
                    }
                }

                dto.setProvinceName(province);
                dto.setCityName(city);
                dto.setRegionName(region);

                result.put("address", dto);
            }
        }

        return getSuccessResult(result);
    }
}
