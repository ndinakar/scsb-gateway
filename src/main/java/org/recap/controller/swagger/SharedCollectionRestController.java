package org.recap.controller.swagger;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.model.AccessionRequest;
import org.recap.model.BibItemAvailabityStatusRequest;
import org.recap.model.DeAccessionRequest;
import org.recap.model.ItemAvailabityStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@RestController
@RequestMapping("/sharedCollection")
@Api(value = "sharedCollection")
public class SharedCollectionRestController {

    private static final Logger logger = LoggerFactory.getLogger(SharedCollectionRestController.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClientUrl;

    @Value("${scsb.circ.url}")
    String scsbCircUrl;

    public String getServerProtocol() {
        return serverProtocol;
    }

    public String getScsbCircUrl() {
        return scsbCircUrl;
    }

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public String getScsbSolrClientUrl() {
        return scsbSolrClientUrl;
    }

    @RequestMapping(value = "/itemAvailabilityStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "itemAvailabilityStatus",
            notes = "Item Availability Status", nickname = "itemAvailabilityStatus")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @ResponseBody
    public ResponseEntity itemAvailabilityStatus(@ApiParam(value = "Item Barcodes with ',' separated", required = true, name = "itemBarcodes") @RequestBody ItemAvailabityStatusRequest itemAvailabityStatus) {
        String response;
        try {
            response = getRestTemplate().postForObject(getServerProtocol() + getScsbSolrClientUrl() + "/sharedCollection/itemAvailabilityStatus", itemAvailabityStatus, String.class);
        } catch (Exception exception) {
            logger.error(ReCAPConstants.LOG_ERROR, exception);
            return new ResponseEntity(ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (StringUtils.isEmpty(response)) {
            return new ResponseEntity(ReCAPConstants.ITEM_BARCDE_DOESNOT_EXIST, getHttpHeaders(), HttpStatus.OK);
        } else {
            return new ResponseEntity(response, getHttpHeaders(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/bibAvailabilityStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "bibAvailabilityStatus",
            notes = "Bibliography Availability Status", nickname = "bibAvailabilityStatus")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),@ApiResponse(code = 503, message = "Service Not Available")})
    @ResponseBody
    public ResponseEntity bibAvailabilityStatus(@ApiParam(value = "Owning Inst BibID, or SCSB BibId", required = true, name = "") @RequestBody BibItemAvailabityStatusRequest bibItemAvailabityStatusRequest) {
        String response;
        try {
            response = getRestTemplate().postForObject(getServerProtocol() + getScsbSolrClientUrl() + "/sharedCollection/bibAvailabilityStatus", bibItemAvailabityStatusRequest, String.class);
        } catch (Exception exception) {
            logger.error(ReCAPConstants.LOG_ERROR, exception);
            return new ResponseEntity(ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.OK);
        }
        return new ResponseEntity(response, getHttpHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deAccession", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "deaccession",
            notes = "Deaccession", nickname = "deaccession")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @ResponseBody
    public ResponseEntity deAccession(@ApiParam(value = "Provide item barcodes to be deaccessioned, separated by comma", required = true, name = "itemBarcodes") @RequestBody DeAccessionRequest deAccessionRequest) {
        try {
            Map<String, String> resultMap = getRestTemplate().postForObject(getServerProtocol() + getScsbCircUrl() + "/sharedCollection/deAccession", deAccessionRequest, Map.class);
            return new ResponseEntity(resultMap, getHttpHeaders(), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ReCAPConstants.LOG_ERROR, ex);
            return new ResponseEntity(ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @RequestMapping(value = "/accessionBatch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "accessionBatch",
            notes = "Accession Batch", nickname = "accessionBatch")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @ResponseBody
    public ResponseEntity accessionBatch(@ApiParam(value = "Item Barcode and Customer Code", required = true, name = "Item Barcode And Customer Code") @RequestBody List<AccessionRequest> accessionRequestList) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            String responseMessage = getRestTemplate().postForObject(getServerProtocol() + getScsbSolrClientUrl() + "sharedCollection/accessionBatch", accessionRequestList, String.class);
            ResponseEntity responseEntity = new ResponseEntity(responseMessage, getHttpHeaders(), HttpStatus.OK);
            stopWatch.stop();
            logger.info("Total time taken for saving accession request-->{}sec", stopWatch.getTotalTimeSeconds());
            return responseEntity;
        } catch (Exception exception) {
            logger.error(ReCAPConstants.LOG_ERROR, exception);
            return new ResponseEntity(ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @RequestMapping(value = "/accessionImmediate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "accessionImmediate",
            notes = "Accession Immediate", nickname = "accessionImmediate")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @ResponseBody
    public ResponseEntity accessionImmediate(@ApiParam(value = "Item Barcode and Customer Code", required = true, name = "Item Barcode And Customer Code") @RequestBody List<AccessionRequest> accessionRequestList) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ResponseEntity responseEntity;
            List<LinkedHashMap> linkedHashMapList = getRestTemplate().postForObject(getServerProtocol() + getScsbSolrClientUrl() + "sharedCollection/accessionImmediate", accessionRequestList, List.class);
            if (null != linkedHashMapList && linkedHashMapList.get(0).get("message").toString().contains(ReCAPConstants.ONGOING_ACCESSION_LIMIT_EXCEED_MESSAGE)) {
                responseEntity = new ResponseEntity(linkedHashMapList, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            } else {
                responseEntity = new ResponseEntity(linkedHashMapList, getHttpHeaders(), HttpStatus.OK);
            }
            stopWatch.stop();
            logger.info("Total time taken for accession-->{}sec",stopWatch.getTotalTimeSeconds());
            return responseEntity;
        } catch (Exception exception) {
            logger.error(ReCAPConstants.LOG_ERROR, exception);
            return new ResponseEntity(ReCAPConstants.SCSB_SOLR_CLIENT_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @RequestMapping(value = "/submitCollection", method = RequestMethod.POST)
    @ApiOperation(value = "submitCollection",
            notes = "Submit Collection", nickname = "submitCollection")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @ResponseBody
    public ResponseEntity submitCollection(@ApiParam(value = "Provide marc xml or scsb xml format to update the records", required = true, name = "inputRecords") @RequestBody String inputRecords) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        ResponseEntity responseEntity;
        try {
            String response = getRestTemplate().postForObject(getServerProtocol() + getScsbCircUrl() + "sharedCollection/submitCollection", inputRecords, String.class);
            if (response.equalsIgnoreCase(ReCAPConstants.INVALID_MARC_XML_FORMAT_MESSAGE) || response.equalsIgnoreCase(ReCAPConstants.INVALID_SCSB_XML_FORMAT_MESSAGE)
                    || response.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR)) {
                responseEntity = new ResponseEntity(response, getHttpHeaders(), HttpStatus.BAD_REQUEST);
            } else {
                responseEntity = new ResponseEntity(response, getHttpHeaders(), HttpStatus.OK);
            }
            return responseEntity;
        } catch (Exception exception) {
            logger.error(ReCAPConstants.LOG_ERROR, exception);
            responseEntity = new ResponseEntity(ReCAPConstants.SCSB_CIRC_SERVICE_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
            return responseEntity;
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }

}