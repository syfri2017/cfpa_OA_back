package com.syfri.digitalplan.controller.planobject;

import com.syfri.baseapi.model.ResultVO;
import com.syfri.baseapi.utils.EConstants;
import com.syfri.digitalplan.model.buildingzoning.BuildingVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.syfri.digitalplan.model.planobject.ImportantunitsVO;
import com.syfri.digitalplan.service.planobject.ImportantunitsService;
import com.syfri.baseapi.controller.BaseController;
import java.util.List;

@RestController
@RequestMapping("importantunits")
public class ImportantunitsController  extends BaseController<ImportantunitsVO>{

	@Autowired
	private ImportantunitsService importantunitsService;

	@Override
	public ImportantunitsService getBaseService() {
		return this.importantunitsService;
	}

	/**
	 * 通过重点单位 查询包含分区详情
	 */
	@ApiOperation(value="通过重点单位 查询包含分区详情",notes="列表信息")
	@ApiImplicitParam(name="vo",value="重点单位对象")
	@PostMapping("/doFindBuildingDetailsByVo")
	public @ResponseBody ResultVO doFindBuildingDetailsByVo(@RequestBody ImportantunitsVO vo)
	{
		ResultVO resultVO = ResultVO.build();
		try{
			List<BuildingVO> result= importantunitsService.doFindBuildingDetailsByVo(vo);
			resultVO.setResult(result);
		}catch(Exception e){
			logger.error("{}",e.getMessage());
			resultVO.setCode(EConstants.CODE.FAILURE);
		}
		return resultVO;
	}
}