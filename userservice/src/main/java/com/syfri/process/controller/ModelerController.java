package com.syfri.process.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.syfri.common.DefaultVO;
import com.syfri.common.Response;
import com.syfri.process.model.ProcessModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("process/model")
public class ModelerController {

    @Autowired
    RepositoryService repositoryService;


    @Autowired
    ObjectMapper objectMapper;

    /**
     * 新建一个空模型
     * @return 返回模型ID
     */
    @PostMapping
    public Response create(@RequestBody ProcessModel processModel)  {
        //初始化一个空模型
        Response response = Response.build();
        try {

            Model model = repositoryService.newModel();
            model.setName(processModel.getName());
            model.setKey("process");
            model.setMetaInfo(JSON.toJSONString(processModel));

            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace",
                    "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
            response.setData(id);
        }catch (Exception e){
            response.setMessage("创建模型异常");
        }
        return response;
    }


    @GetMapping("/{id}")
    public Response get(@PathVariable("id") String id) {
        Response response = Response.build();
        Model model = repositoryService.createModelQuery().modelId(id).singleResult();
        response.setData(model);

        return response;
    }

    @PostMapping("/list")
    public Response list(@RequestBody DefaultVO vo) {
        Response response = Response.build();
        int pageSize = vo.getPageSize();
        int pageNum = vo.getPageNum();
        int start = pageSize * (pageNum-1);
        List<Model> list = repositoryService.createModelQuery().listPage(start, pageSize);
        long count = repositoryService.createModelQuery().count();
        Map<String,Object> result = new HashMap<>();
        result.put("list",list);
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

    @GetMapping(value="/{modelId}",  produces = "application/json")
    public ObjectNode getEditorJson(@PathVariable("modelId") String modelId) {
        ObjectNode modelNode = null;

        Model model = repositoryService.getModel(modelId);

        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                }
                modelNode.put("modelId", model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(
                        new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.put("model", editorJsonNode);

            } catch (Exception e) {
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;

    }


    @PutMapping("/{modelId}")
    public void saveModel(@PathVariable String modelId, @RequestBody ProcessModel processModel) {
        try {

            Model model = repositoryService.getModel(modelId);

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            Map<String,String> meta = new HashMap<>();
            meta.put("name",processModel.getName());
            meta.put("modelId",processModel.getModelId());
            model.setMetaInfo(JSON.toJSONString(meta));
            model.setName(processModel.getName());

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), processModel.getJsonXml().getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(processModel.getSvgXml().getBytes(
                    "utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();

        } catch (Exception e) {
            throw new ActivitiException("Error saving model", e);
        }
    }

}
