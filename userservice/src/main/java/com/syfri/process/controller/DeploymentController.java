package com.syfri.process.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfri.common.DefaultVO;
import com.syfri.common.Response;
import com.syfri.process.model.DeploymentVO;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("process/deployment")
public class DeploymentController {


    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;


    /**
     * 根据ID获取
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/{id}")
    public Response get(@PathVariable("id") String id) throws Exception {
        Response response = Response.build();
        //获取模型
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(id).singleResult();
        response.setData(deployment);
        return response;
    }


    @DeleteMapping("/{id}")
    public Response delete(@PathVariable("id") String id) {
        Response response = Response.build();
        try {
            repositoryService.deleteDeployment(id);
        }catch (Exception e){
            response.setCode(Response.RESFAIL);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    /**
     * 部署列表
     * @param vo
     * @return
     * @throws Exception
     */
    @PostMapping("/list")
    public Response list(@RequestBody DefaultVO vo) throws Exception {
        Response response = Response.build();
        int pageSize = vo.getPageSize();
        int pageNum = vo.getPageNum();
        int start = pageSize * (pageNum-1);
        //获取模型
        List<Deployment> list = repositoryService.createDeploymentQuery().listPage(start,pageSize);

        long count = repositoryService.createDeploymentQuery().count();
        List<DeploymentVO> nlist = new ArrayList<>();
        for(Deployment deployment : list){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            nlist.add(new DeploymentVO(deployment.getId(),
                    deployment.getName(),
                    format.format(deployment.getDeploymentTime()),
                    deployment.getKey())
            );
        }

        Map<String,Object> result = new HashMap<>();
        result.put("list",nlist);
        result.put("pageSize",pageSize);
        result.put("pageNum",pageNum);
        if(count % pageSize == 0){
            result.put("totalPage",count / pageSize);
        }else{
            result.put("totalPage",count / pageSize + 1);
        }

        result.put("totalCount",count);
        response.setData(result);
        return response;
    }

    /**
     * 发布模型为流程定义
     * @param modelId
     * @return
     * @throws Exception
     */
    @PostMapping("/deploy")
    public Response deploy(@RequestBody String modelId) throws Exception {
        Response response = Response.build();
        //获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            response.setCode(Response.RESFAIL);
            response.setMessage("模型数据为空，请先设计流程并成功保存，再进行发布。");
            return response;
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            response.setCode(Response.RESFAIL);
            response.setMessage("数据模型不符要求，请至少设计一条主线流程");
            return response;
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return response;
    }

}
