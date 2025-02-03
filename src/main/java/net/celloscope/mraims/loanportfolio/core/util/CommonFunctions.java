package net.celloscope.mraims.loanportfolio.core.util;

import com.google.gson.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.DateTimeFormatterPattern;
import net.celloscope.mraims.loanportfolio.core.util.enums.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UtilityClass
@Slf4j
public class CommonFunctions {
	public String buildGsonBuilder(Object object) {
		return buildGson(object).toJson(object);
	}

	public Gson buildGson(Object object) {
		DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
		return new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class,
						(JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), formater))
				.registerTypeAdapter(LocalDateTime.class,
						(JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(formater)))
				.registerTypeAdapter(LocalDate.class,
						(JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
								DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.registerTypeAdapter(LocalDate.class,
						(JsonSerializer<LocalDate>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.setPrettyPrinting().create();
	}

	public Gson buildGsonExcludingProperties(Object object) {
		DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
		return new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class,
						(JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), formater))
				.registerTypeAdapter(LocalDateTime.class,
						(JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(formater)))
				.registerTypeAdapter(LocalDate.class,
						(JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
								DateTimeFormatter.ofPattern("yyyy-MM-dd")))
				.registerTypeAdapter(LocalDate.class,
						(JsonSerializer<LocalDate>) (localDateTime, type, jsonSerializationContext) ->
								new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.setPrettyPrinting()
				.setExclusionStrategies(new ExcludeOidExclusionStrategy())
				.create();
	}

	public BigDecimal round(BigDecimal amount, int scale, RoundingMode roundingMode) {
		return amount.setScale(scale, roundingMode);
	}

	public static <T> String getFieldValueByObjectAndFieldName(T object, String fieldName) {
		String fieldValue = null;
		try {
			Class<?> objectClass = object.getClass();
			Field field = objectClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			fieldValue = field.get(object) == null ? null : field.get(object).toString();
//			log.info("field Name : {}, value of field : {}",fieldName, fieldValue);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("Error : Field : {} not found or inaccessible", fieldName);
		}
		return fieldValue;
	}


	public RoundingMode getRoundingMode(String roundingLogic) {
		MetaPropertyEnum metaPropertyEnum = Arrays.stream(MetaPropertyEnum.values())
				.filter(e -> e.getValue().equals(roundingLogic.toUpperCase()))
				.findFirst()
				.orElse(null);

		RoundingMode roundingMode = null;
		if (metaPropertyEnum != null) {
			switch (Objects.requireNonNull(metaPropertyEnum)) {
				case ROUNDING_MODE_HALF_UP -> roundingMode = RoundingMode.HALF_UP;
				case ROUNDING_MODE_HALF_DOWN -> roundingMode = RoundingMode.HALF_DOWN;
				case ROUNDING_MODE_UP -> roundingMode = RoundingMode.UP;
				case ROUNDING_MODE_DOWN -> roundingMode = RoundingMode.DOWN;
			}
		}
		return roundingMode;
	}


	public static <T> Mono<T> checkAccessibilityAndValidateRequestDTO(T validationRequestDTO, List<String> queryParamsList, List<String> userAccessRolesList){
		final List<String> requiredHeaderFieldList = List.of(
				QueryParams.MFI_ID.getValue(),
				QueryParams.LOGIN_ID.getValue(),
//                QueryParams.OFFICE_ID.getValue(),
				QueryParams.USER_ROLE.getValue()
		);

		final String providedUserRole = HelperUtil.getFieldValueFromObject(validationRequestDTO, QueryParams.USER_ROLE.getValue());

		return Mono.fromSupplier(() -> validationRequestDTO)
				.filter(requestDTO -> HelperUtil.validateRequestDTO(requestDTO, requiredHeaderFieldList))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Validate Request")))
				.filter(requestDTO -> HelperUtil.validateRequestDTO(requestDTO, queryParamsList))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Please Provide Required Parameters: " + queryParamsList)))
//				.filter(requestDTO -> userAccessRolesList.stream().anyMatch(userRole -> !HelperUtil.checkIfNullOrEmpty(providedUserRole) && userRole.equals(providedUserRole)))
//				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.FORBIDDEN, "User does not have access to the requested resource")))
				;
	}

	public BigDecimal getAnnualInterestRate(BigDecimal interestRate, String interestRateFrequency) {
		BigDecimal annualInterestRate;
		switch (interestRateFrequency.toUpperCase()) {
			case "YEARLY" -> annualInterestRate = interestRate;
			case "MONTHLY" -> annualInterestRate = interestRate.multiply(BigDecimal.valueOf(12));
			default -> annualInterestRate = interestRate;
		}
		return annualInterestRate;
	}

	public List<Integer> getInterestPostingMonthListByInterestPostingPeriod(String interestPostingPeriod) {
		List<Integer> interestPostingMonthList = null;
		switch (interestPostingPeriod.toUpperCase()) {
			case "MONTHLY" -> interestPostingMonthList = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
			case "QUARTERLY" -> interestPostingMonthList = List.of(3, 6, 9, 12);
			case "HALF_YEARLY" -> interestPostingMonthList = List.of(6, 12);
			case "YEARLY" -> interestPostingMonthList = List.of(12);
			default -> interestPostingMonthList = List.of(6, 12);
		}
		return interestPostingMonthList;
	}

	public BigDecimal calculateMaturityAmountForWeeklyDepositDPS(double weeklyDeposit, double annualInterestRate, int dpsPeriodWeeks) {
		double weeklyInterestRate = annualInterestRate/100/52;
		double weeklyInterestRateRounded = BigDecimal.valueOf(weeklyInterestRate).setScale(8, RoundingMode.HALF_UP).doubleValue();
		double maturityAmount = weeklyDeposit * (((Math.pow(1 + weeklyInterestRateRounded, dpsPeriodWeeks) - 1) / weeklyInterestRateRounded) + Math.pow(1 + weeklyInterestRateRounded, dpsPeriodWeeks));
		maturityAmount = maturityAmount - weeklyDeposit;

		System.out.println("maturity amount weekly : " + maturityAmount);
		return BigDecimal.valueOf(maturityAmount).setScale(0, RoundingMode.HALF_UP);
	}

	public BigDecimal calculateMaturityAmountMonthlyDepositDPS(double monthlyDeposit, double annualInterestRate, int dpsPeriodMonths) {
		double monthlyInterestRate = annualInterestRate/100/12;
		double dailyInterestRate = annualInterestRate/100/366;
		double monthlyInterestRateRounded = BigDecimal.valueOf(monthlyInterestRate).setScale(8, RoundingMode.HALF_UP).doubleValue();
		double dailyInterestRateRounded = BigDecimal.valueOf(dailyInterestRate).setScale(8, RoundingMode.HALF_UP).doubleValue();
		log.info("monthlyInterestRateRounded : {}", monthlyInterestRateRounded);

		double maturityAmount = monthlyDeposit * (((Math.pow(1 + monthlyInterestRateRounded, dpsPeriodMonths) - 1) / monthlyInterestRateRounded) + Math.pow(1 + monthlyInterestRateRounded, dpsPeriodMonths));
		double maturityAmountDailyInterestRate = monthlyDeposit * (((Math.pow(1 + dailyInterestRateRounded, dpsPeriodMonths) - 1) / dailyInterestRateRounded) + Math.pow(1 + dailyInterestRateRounded, dpsPeriodMonths));
		log.info("maturityAmount : {}", maturityAmount);
		maturityAmount = maturityAmount - monthlyDeposit;
		double maturityAmountDaily = maturityAmountDailyInterestRate - monthlyDeposit;

		System.out.println("maturityAmount monthly: " + maturityAmount);
		System.out.println("maturityAmount daily: " + maturityAmountDaily);
		return BigDecimal.valueOf(maturityAmount).setScale(0, RoundingMode.HALF_UP);
	}

	public MetaProperty getMetaPropertyFromJson(String metaPropertyJsonString) {

			/*Gson gson = new Gson();
			Type listType = new TypeToken<List<JsonProperty>>(){}.getType();
			List<JsonProperty> jsonProperties = gson.fromJson(metaPropertyJsonString, listType);

			MetaProperty metaProperty = new MetaProperty();
			for (JsonProperty jsonProperty : jsonProperties) {
				MetaPropertyEnum metaPropertyEnum = Arrays.stream(MetaPropertyEnum.values())
						.filter(e -> e.getValue().equals(jsonProperty.getName()))
						.findFirst()
						.orElse(null);

				if (metaPropertyEnum != null) {
					switch (metaPropertyEnum) {
						case INSTALLMENT_PRECISION ->
							metaProperty.setInstallmentPrecision(new BigDecimal(jsonProperty.getValue()));
						case SERVICE_CHARGE_PRECISION ->
							metaProperty.setServiceChargePrecision(new BigDecimal(jsonProperty.getValue()));
						case ROUNDING_TO ->
							metaProperty.setRoundingTo(new BigDecimal(jsonProperty.getValue()));
						case ROUNDING_LOGIC ->
							metaProperty.setRoundingLogic(getRoundingMode(jsonProperty.getValue()));
					}
				}
			}

			return metaProperty;*/
		return null;
	}


	public void validateHeaders(ServerRequest request, String header, String message) {
		if (request.headers().header(header).isEmpty()) {
			log.error("error occurred while checking headers: {}", header);
			throw new ServerWebInputException(message);
		}
		if (StringUtils.isBlank(request.headers().firstHeader(header))) {
			log.error("error occurred while checking headers: {}", header);
			throw new ServerWebInputException(message);
		}
	}

	public static BigDecimal getMaximumEqualInstallmentAmountDeclining(BigDecimal selectedEi, Integer noOfInstallments, BigDecimal loanAmount, Double serviceChargeRatePerPeriod) {
		List<BigDecimal> possibleEqualInstallments = new ArrayList<>();

		BigDecimal maximumEI = selectedEi.add(BigDecimal.ONE);
		while (true) {
			double remainingBalance = loanAmount.doubleValue();
			int count = 0;
			while (remainingBalance > 0 && count < noOfInstallments) {
				double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
				double calculatedPrincipal = maximumEI.doubleValue() - interestAmount;
				remainingBalance = remainingBalance - calculatedPrincipal;
				count++;
			}

			if (count == noOfInstallments) {
				possibleEqualInstallments.add(maximumEI);
				maximumEI = maximumEI.add(BigDecimal.ONE);
			} else {
				break;
			}
		}

		System.out.println("Maximum EI List : " + possibleEqualInstallments);
		return possibleEqualInstallments.isEmpty() ? selectedEi : possibleEqualInstallments.get(possibleEqualInstallments.size() - 1);
	}


	public static BigDecimal getMinimumEqualInstallmentAmountDeclining(BigDecimal selectedEi, Integer noOfInstallments, BigDecimal loanAmount, Double serviceChargeRatePerPeriod, Double deviationPercentage) {
		List<BigDecimal> possibleEqualInstallments = new ArrayList<>();
		BigDecimal maximumLastInstallment = selectedEi.add(selectedEi.multiply(BigDecimal.valueOf(deviationPercentage / 100)).setScale(0, RoundingMode.DOWN));
		BigDecimal minimumEi = selectedEi.subtract(BigDecimal.ONE);
		while (true) {
			double remainingBalance = loanAmount.doubleValue();
			BigDecimal lastInstallmentAmount = BigDecimal.ZERO;
			int count = 0;
			double calculatedPrincipal = 0;
			while (count < noOfInstallments) {
				if (count == noOfInstallments - 1) {
					lastInstallmentAmount = BigDecimal.valueOf((remainingBalance * serviceChargeRatePerPeriod) + remainingBalance);
				} else {
					double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
					calculatedPrincipal = minimumEi.doubleValue() - interestAmount;
					remainingBalance = remainingBalance - calculatedPrincipal;
				}
				count++;
			}

			if (lastInstallmentAmount.doubleValue() <= maximumLastInstallment.doubleValue()) {
				possibleEqualInstallments.add(minimumEi);
				minimumEi = minimumEi.subtract(BigDecimal.ONE);
			} else {
				break;
			}
		}

		System.out.println("Minimum EI List : " + possibleEqualInstallments);
		return possibleEqualInstallments.isEmpty() ? selectedEi : possibleEqualInstallments.get(possibleEqualInstallments.size() - 1);
	}

	public static BigDecimal getMaximumEqualInstallmentAmountFlat(BigDecimal calculatedEi, Integer noOfInstallments, BigDecimal cumulativeAmount) {
		int installmentCountThreshold = noOfInstallments-1;
		List<BigDecimal> maximumEIList = new ArrayList<>();
		BigDecimal maximumEI = calculatedEi.add(BigDecimal.ONE);
		log.info("installmentCountThreshold: {}", installmentCountThreshold);

		while (cumulativeAmount.doubleValue() / maximumEI.doubleValue() > installmentCountThreshold) {
			maximumEIList.add(maximumEI);
			maximumEI = maximumEI.add(BigDecimal.ONE);
		}

		maximumEI = maximumEIList.isEmpty() ? calculatedEi : maximumEIList.get(maximumEIList.size()-1);
		return maximumEI;
	}

	public static BigDecimal getMinimumEqualInstallmentAmountFlat(BigDecimal calculatedEi, Integer noOfInstallments, BigDecimal cumulativeAmount, Double deviationPercentage) {
		BigDecimal maximumLastInstallment = calculatedEi.add(calculatedEi.multiply(BigDecimal.valueOf(deviationPercentage / 100)).setScale(0, RoundingMode.DOWN));
		log.info("maximumLastInstallment: {}", maximumLastInstallment);
		List<BigDecimal> minimumEIList = new ArrayList<>();
		BigDecimal minimumEI = calculatedEi.subtract(BigDecimal.ONE);

		while (cumulativeAmount.doubleValue() - (minimumEI.doubleValue() * (noOfInstallments - 1)) <= maximumLastInstallment.doubleValue()) {
			minimumEIList.add(minimumEI);
			minimumEI = minimumEI.subtract(BigDecimal.ONE);
		}

		minimumEI = minimumEIList.isEmpty() ? calculatedEi : minimumEIList.get(minimumEIList.size()-1);
		return minimumEI;
	}

}
