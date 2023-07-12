package io.mosip.authentication.fw.util;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Reporter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.authentication.fw.dto.OutputValidationDto;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.precon.MessagePrecondtion;
import io.mosip.global.utils.GlobalConstants;

/**
 * Perform output validation between expected and actual json file or message
 * 
 * @author Vignesh
 *
 */
public class OutputValidationUtil extends AuthTestsUtil{

	private static final Logger OUTPUTVALIDATION_LOGGER = Logger.getLogger(OutputValidationUtil.class);
	
	/**
	 * The method will perform output validation by comparing expected and actual value
	 * 
	 * @param actualOutputFile
	 * @param expOutputFile
	 * @return map of ouptut validation report
	 */
	public static Map<String, List<OutputValidationDto>> doOutputValidation(String actualOutputFile,
			String expOutputFile) {
		try {
			Map<String, String> actual = MessagePrecondtion.getPrecondtionObject(actualOutputFile)
					.retrieveMappingAndItsValueToPerformOutputValidation(actualOutputFile);
			Map<String, String> exp = MessagePrecondtion.getPrecondtionObject(expOutputFile)
					.retrieveMappingAndItsValueToPerformOutputValidation(expOutputFile);
			actualOutputFile = actualOutputFile.substring(actualOutputFile.lastIndexOf("/") + 1,
					actualOutputFile.length());
			expOutputFile = expOutputFile.substring(expOutputFile.lastIndexOf("/") + 1, expOutputFile.length());
			return compareActuExpValue(actual, exp, actualOutputFile + " vs " + expOutputFile);
		} catch (Exception e) {
			OUTPUTVALIDATION_LOGGER.error("Exceptione occured " + e.getMessage());
			return Collections.emptyMap();
		}
	}
	
	/**
	 * The method will compare expected and actual value
	 * 
	 * @param actual
	 * @param exp
	 * @param actVsExp
	 * @return map
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static Map<String, List<OutputValidationDto>> compareActuExpValue(Map<String, String> actual,
			Map<String, String> exp, String actVsExp) {
		Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
		List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
		try {
			for (Entry<String, String> actualEntry : actual.entrySet()) {
				OutputValidationDto objOpDto = new OutputValidationDto();
				if (!exp.containsKey(actualEntry.getKey())) {
					objOpDto.setFieldName(actualEntry.getKey());
					objOpDto.setFieldHierarchy(actualEntry.getKey());
					objOpDto.setActualValue(actualEntry.getValue());
					objOpDto.setExpValue("NOT VERIFIED");
					objOpDto.setStatus("WARNING");
					objList.add(objOpDto);
				}
			}
			for (Entry<String, String> expEntry : exp.entrySet()) {
				OutputValidationDto objOpDto = new OutputValidationDto();
				if (actual.containsKey(expEntry.getKey())) {
					if (!expEntry.getValue().equals("$IGNORE$") && !expEntry.getValue().contains("$DECODE$")) {
						if (expEntry.getValue().equals(actual.get(expEntry.getKey()))) {
							objOpDto.setFieldName(expEntry.getKey());
							objOpDto.setFieldHierarchy(expEntry.getKey());
							objOpDto.setActualValue(actual.get(expEntry.getKey()));
							objOpDto.setExpValue(expEntry.getValue());
							objOpDto.setStatus("PASS");
						} else if (expEntry.getValue().equals("$TIMESTAMP$")) {
							if (validateTimestampZ(actual.get(expEntry.getKey()))) {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus("PASS");
							} else {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus(GlobalConstants.FAIL_STRING);
							}
						} else if (expEntry.getValue().equals("$TIMESTAMPZ$")) {
							if (validateTimestampZ(actual.get(expEntry.getKey()))) {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus("PASS");
							} else {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus(GlobalConstants.FAIL_STRING);
							}
						} else if (expEntry.getValue().contains(GlobalConstants.TOKENID_STRING) && expEntry.getValue().contains(".")) {
							String key = expEntry.getValue().replace(GlobalConstants.TOKENID_STRING, "");
							String[] keys = key.split(Pattern.quote("."));
							String tokenid = RunConfigUtil.getTokenId(keys[0], keys[1]);
							if (!tokenid.contains("TOKENID")) {
								if (tokenid.equals(actual.get(expEntry.getKey()))) {
									objOpDto.setFieldName(expEntry.getKey());
									objOpDto.setFieldHierarchy(expEntry.getKey());
									objOpDto.setActualValue(actual.get(expEntry.getKey()));
									objOpDto.setExpValue(expEntry.getValue());
									objOpDto.setStatus("PASS");
								} else {
									objOpDto.setFieldName(expEntry.getKey());
									objOpDto.setFieldHierarchy(expEntry.getKey());
									objOpDto.setActualValue(actual.get(expEntry.getKey()));
									objOpDto.setExpValue(expEntry.getValue());
									objOpDto.setStatus(GlobalConstants.FAIL_STRING);
								}
							} else if (tokenid.contains("TOKENID")) {
								tokenid = tokenid.replace(GlobalConstants.TOKENID_STRING, "");
								String[] values = tokenid.split(Pattern.quote("."));
								String id = values[0];
								String pid = values[1];
								performTokenIdOper(id, pid, actual.get(expEntry.getKey()));
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus("PASS");
							}
						} else if (expEntry.getValue().contains("$REGEXP")) {
							String extractRegex = expEntry.getValue().replace("$", "");
							String[] array = extractRegex.split(":");
							String regex = array[1];
							if (validateRegularExpression(actual.get(expEntry.getKey()), regex)) {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus("PASS");
							} else {
								objOpDto.setFieldName(expEntry.getKey());
								objOpDto.setFieldHierarchy(expEntry.getKey());
								objOpDto.setActualValue(actual.get(expEntry.getKey()));
								objOpDto.setExpValue(expEntry.getValue());
								objOpDto.setStatus(GlobalConstants.FAIL_STRING);
							}
						} else {
							objOpDto.setFieldName(expEntry.getKey());
							objOpDto.setFieldHierarchy(expEntry.getKey());
							objOpDto.setActualValue(actual.get(expEntry.getKey()));
							objOpDto.setExpValue(expEntry.getValue());
							objOpDto.setStatus(GlobalConstants.FAIL_STRING);
						}
						objList.add(objOpDto);
					} else if (expEntry.getValue().contains("$DECODE$")) {
						String keyword = expEntry.getValue();
						String content = actual.get(expEntry.getKey());
						String expKeyword = keyword.substring(keyword.lastIndexOf("->") + 2, keyword.length());
						String actKeyword = expKeyword.replace("expected", "actual");
						Map<String, Object> actualMap = JsonPrecondtion.jsonToMap(new JSONObject(
								JsonPrecondtion.getJsonInOrder(EncryptDecrptUtil.getDecyptFromStr(content))));
						Map<String, Object> expMap = null;
						expMap = JsonPrecondtion.jsonToMap(
								new JSONObject(getContentFromFile(FileUtil.getFilePath(getTestFolder(), expKeyword))));
						FileUtil.createAndWriteFile(actKeyword,
								JsonPrecondtion.getJsonInOrder(EncryptDecrptUtil.getDecyptFromStr(content)));
						if (compareTwoKycMap(expMap, actualMap)) {
							Reporter.log("Kyc verification passed");
							OUTPUTVALIDATION_LOGGER.info("Kyc Verification Passed \\n Expected Kyc: " + expMap + "\\n Actual Kyc: " + actualMap);
							objOpDto.setFieldName(expEntry.getKey());
							objOpDto.setFieldHierarchy(expEntry.getKey());
							objOpDto.setActualValue(actualMap.toString());
							objOpDto.setExpValue(expMap.toString());
							objOpDto.setStatus("PASS");
						} else {
							Reporter.log("Kyc verification failed");
							OUTPUTVALIDATION_LOGGER.error("Kyc Verification failed \\n Expected Kyc: " + expMap + "\\n Actual Kyc: " + actualMap);
							objOpDto.setFieldName(expEntry.getKey());
							objOpDto.setFieldHierarchy(expEntry.getKey());
							objOpDto.setActualValue(actualMap.toString());
							objOpDto.setExpValue(expMap.toString());
							objOpDto.setStatus(GlobalConstants.FAIL_STRING);
						}
					}
				} else if (!expEntry.getValue().equals("$IGNORE$")) {
					objOpDto.setFieldName(expEntry.getKey());
					objOpDto.setFieldHierarchy(expEntry.getKey());
					objOpDto.setActualValue("NOT AVAILABLE");
					objOpDto.setExpValue(expEntry.getValue());
					objOpDto.setStatus(GlobalConstants.FAIL_STRING);
					objList.add(objOpDto);
					OUTPUTVALIDATION_LOGGER
							.error("The expected json path " + expEntry.getKey() + " is not available in actual json");
				}
			}
			objMap.put(actVsExp, objList);
		} catch (JSONException | IOException e) {
			OUTPUTVALIDATION_LOGGER.error("Kyc Verification failed " + e.getMessage());
		}
		return objMap;
	}
	
	/**
	 * The method will validate timestamp
	 * 
	 * @param timestamp
	 * @return true or false
	 */
	public static boolean validateTimestamp(String timestamp) {
		try {
			Date date = new Date();
			long time = date.getTime();
			Timestamp ts = new Timestamp(time);
			String currentTimeStamp = ts.toString();
			if (!timestamp.substring(0, 4).equals(currentTimeStamp.substring(0, 4)))
				return false;
			if (!timestamp.substring(4, 5).equals("-"))
				return false;
			if (!(new Integer(timestamp.substring(5, 7)) <= 12))
				return false;
			if (!timestamp.substring(7, 8).equals("-"))
				return false;
			if (!(new Integer(timestamp.substring(8, 10)) <= 31))
				return false;
			if (!timestamp.substring(10, 11).equals("T"))
				return false;
			if (!(new Integer(timestamp.substring(11, 13)) <= 24))
				return false;
			if (!timestamp.substring(13, 14).equals(":"))
				return false;
			if (!(new Integer(timestamp.substring(14, 16)) <= 59))
				return false;
			if (!timestamp.substring(16, 17).equals(":"))
				return false;
			if (!(new Integer(timestamp.substring(17, 19)) <= 59))
				return false;
			if (!timestamp.substring(19, 20).equals("."))
				return false;
			if (!timestamp.substring(23, 24).equals("+"))
				return false;
			if (!timestamp.substring(26, 27).equals(":"))
				return false;
			return true;
		} catch (Exception e) {
			OUTPUTVALIDATION_LOGGER.error(e.getMessage());
			return false;
		}
	}
	
	/**
	 * The methold will validate timestamp with Z format
	 * 
	 * @param timestamp 
	 * @return true or false
	 */
	public static boolean validateTimestampZ(String timestamp) {
		try {
			Date date = new Date();
			long time = date.getTime();
			Timestamp ts = new Timestamp(time);
			String currentTimeStamp = ts.toString();
			if (!timestamp.substring(0, 4).equals(currentTimeStamp.substring(0, 4)))
				return false;
			if (!timestamp.substring(4, 5).equals("-"))
				return false;
			if (!(new Integer(timestamp.substring(5, 7)) <= 12))
				return false;
			if (!timestamp.substring(7, 8).equals("-"))
				return false;
			if (!(new Integer(timestamp.substring(8, 10)) <= 31))
				return false;
			if (!timestamp.substring(10, 11).equals("T"))
				return false;
			if (!(new Integer(timestamp.substring(11, 13)) <= 24))
				return false;
			if (!timestamp.substring(13, 14).equals(":"))
				return false;
			if (!(new Integer(timestamp.substring(14, 16)) <= 59))
				return false;
			if (!timestamp.substring(16, 17).equals(":"))
				return false;
			if (!(new Integer(timestamp.substring(17, 19)) <= 59))
				return false;
			if (!timestamp.substring(19, 20).equals("."))
				return false;
			if (!timestamp.substring(23, 24).equals("Z"))
				return false;
			return true;
		} catch (Exception e) {
			OUTPUTVALIDATION_LOGGER.error(e.getMessage());
			return false;
		}
	}
	
	/**
	 * The method will validate regular expression
	 * 
	 * @param actValue
	 * @param regex
	 * @return true or false
	 */
	public static boolean validateRegularExpression(String actValue, String regex) {
		if (Pattern.matches(regex, actValue))
			return true;
		else
			return false;
	}
	
	/**
	 * The method will publish report
	 * 
	 * @param outputresult
	 * @return true or false
	 */
	public static boolean publishOutputResult(Map<String, List<OutputValidationDto>> outputresult) {
		boolean outputStatus = true;
		OUTPUTVALIDATION_LOGGER.info(
				"*******************************************Output validation*******************************************");
		for (Entry<String, List<OutputValidationDto>> entry : outputresult.entrySet()) {
			OUTPUTVALIDATION_LOGGER.info("* OutputValidaiton For : " + entry.getKey());
			for (OutputValidationDto dto : entry.getValue()) {
				OUTPUTVALIDATION_LOGGER.info("*");
				if (dto.getStatus().equals("PASS")) {
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.JSONFIELD_PATH_STRING + dto.getFieldName());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.EXPECTED_VALUE_STRING + dto.getExpValue());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.ACTUAL_VALUE_STRING + dto.getActualValue());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.STATUS_STRING + dto.getStatus());
				}else if (dto.getStatus().equals("WARNING")) {
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.JSONFIELD_PATH_STRING + dto.getFieldName());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.EXPECTED_VALUE_STRING + dto.getExpValue());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.ACTUAL_VALUE_STRING + dto.getActualValue());
					OUTPUTVALIDATION_LOGGER.info(GlobalConstants.STATUS_STRING + dto.getStatus());
				}else if (dto.getStatus().equals(GlobalConstants.FAIL_STRING)) {
					OUTPUTVALIDATION_LOGGER.error(GlobalConstants.JSONFIELD_PATH_STRING + dto.getFieldName());
					OUTPUTVALIDATION_LOGGER.error(GlobalConstants.EXPECTED_VALUE_STRING + dto.getExpValue());
					OUTPUTVALIDATION_LOGGER.error(GlobalConstants.ACTUAL_VALUE_STRING + dto.getActualValue());
					OUTPUTVALIDATION_LOGGER.error(GlobalConstants.STATUS_STRING + dto.getStatus());
					outputStatus = false;
				}
			}
		}
		OUTPUTVALIDATION_LOGGER.info(
				"*******************************************************************************************************");
		return outputStatus;
	}
	
	/**
	 * The method will perform token id operation for uin
	 * 
	 * @param uin
	 * @param tspId
	 * @param tokenId
	 */
	public static void performTokenIdOper(String uin, String tspId, String tokenId) {
		File file = new File(
				new File(RunConfigUtil.getResourcePath() + RunConfigUtil.getTokenIdPropertyPath()).getAbsolutePath());
		if (file.exists()) {
			if (!getPropertyFromFilePath(file.getAbsolutePath()).containsKey(uin + "." + tspId)) {
				Map<String, String> map = getPropertyAsMap(file.getAbsolutePath());
				map.put(uin + "." + tspId, tokenId);
				generateMappingDic(file.getAbsolutePath(), map);
			}
		} else {
			Map<String, String> map = getPropertyAsMap(file.getAbsolutePath());
			map.put(uin + "." + tspId, tokenId);
			generateMappingDic(file.getAbsolutePath(), map);
		}
	}
	
	public static boolean compareTwoKycMap(Map<String, Object> expMap, Map<String, Object> actualMap) {
		for (Entry<String, Object> entry : expMap.entrySet()) {
			if (actualMap.containsKey(entry.getKey())) {
				
				if(entry.getValue()==null || actualMap.get(entry.getKey())==null)
					continue;
				try {
				if (actualMap.get(entry.getKey()).toString().contains(",")
						&& entry.getValue().toString().contains(",")) {
					String value[] = entry.getValue().toString().split(Pattern.quote("}, {"));
					for (int i = 0; i < value.length; i++) {
						String normalise = value[i].replace("{", "").replace("[", "").replace("}", "")
								.replace("]", "");
						if (!actualMap.get(entry.getKey()).toString().contains(normalise)) {
							return false;
						}
					}
				}
				else if (!actualMap.get(entry.getKey()).equals(entry.getValue())) {
					return false;
				}
				}catch(Exception e)
				{
					OUTPUTVALIDATION_LOGGER.error(e.getStackTrace());
				}
			}
		}
		return true;
	}
	public static Map<String, List<OutputValidationDto>> doJsonOutputValidation(String actualOutputJson,
			String expOutputJson) {
		try {
			JsonPrecondtion jsonPrecondtion = new JsonPrecondtion();
			Map<String, String> actual = jsonPrecondtion.retrieveMappingAndItsValueToPerformJsonOutputValidation(actualOutputJson);
			Map<String, String> exp = jsonPrecondtion.retrieveMappingAndItsValueToPerformJsonOutputValidation(expOutputJson);
			return compareActuExpValue(actual, exp, "expected vs actual");
		} catch (Exception e) {
			OUTPUTVALIDATION_LOGGER.error("Exceptione occured " + e.getMessage());
			return Collections.emptyMap();
		}
	}
	
}
