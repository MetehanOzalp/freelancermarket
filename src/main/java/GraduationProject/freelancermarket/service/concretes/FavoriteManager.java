package GraduationProject.freelancermarket.service.concretes;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import GraduationProject.freelancermarket.core.business.BusinessRules;
import GraduationProject.freelancermarket.entities.Favorite;
import GraduationProject.freelancermarket.model.dto.FavoriteAddDto;
import GraduationProject.freelancermarket.repository.FavoriteRepository;
import GraduationProject.freelancermarket.service.abstracts.FavoriteService;
import GraduationProject.freelancermarket.service.abstracts.TokenUserNameAndIdValidationService;
import GraduationProject.freelancermarket.utils.DataResult;
import GraduationProject.freelancermarket.utils.ErrorDataResult;
import GraduationProject.freelancermarket.utils.ErrorResult;
import GraduationProject.freelancermarket.utils.Result;
import GraduationProject.freelancermarket.utils.SuccessDataResult;
import GraduationProject.freelancermarket.utils.SuccessResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteManager implements FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final TokenUserNameAndIdValidationService tokenUserNameAndIdValidationService;
	private final ModelMapper modelMapper;

	@Override
	public Result add(FavoriteAddDto favoriteAddDto) {
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(favoriteAddDto.getUserId()),
				checkIfAlreadyExists(favoriteAddDto));
		if (businessRules != null) {
			return new ErrorResult(businessRules.getMessage());
		}
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		Favorite favorite = modelMapper.map(favoriteAddDto, Favorite.class);
		favoriteRepository.save(favorite);
		return new SuccessResult("Favorilere eklendi");
	}

	@Override
	public Result delete(int userId, int advertId) {
		var favorite = favoriteRepository.findByUserIdAndAdvertId(userId, advertId);
		if (favorite == null) {
			return new ErrorResult("Favori bulunamadı");
		}
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(favorite.getUserId()));
		if (businessRules != null) {
			return new ErrorResult(businessRules.getMessage());
		}
		favoriteRepository.deleteById(favorite.getId());
		return new SuccessResult("Favorilerden silindi");
	}

	@Override
	public DataResult<List<Favorite>> getByUserId(int id) {
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(id));
		if (businessRules != null) {
			return new ErrorDataResult<List<Favorite>>(businessRules.getMessage());
		}
		return new SuccessDataResult<List<Favorite>>(favoriteRepository.findByUserId(id), "Favoriler listelendi");
	}

	public Result userIdAndTokenUserNameVerification(int userId) {
		var result = tokenUserNameAndIdValidationService.userIdAndTokenUserNameVerification(userId);
		if (!result.isSuccess()) {
			return new ErrorResult(result.getMessage());
		}
		return new SuccessResult();
	}

	public Result checkIfAlreadyExists(FavoriteAddDto favoriteAddDto) {
		var result = getByUserId(favoriteAddDto.getUserId());
		for (int i = 0; i < result.getData().size(); i++) {
			if (result.getData().get(i).getAdvertId() == favoriteAddDto.getAdvertId()) {
				return new ErrorResult("Zaten favorilere eklendi");
			}
		}
		return new SuccessResult();
	}

}
