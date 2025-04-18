package GraduationProject.freelancermarket.service.concretes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import GraduationProject.freelancermarket.core.adapters.image.CloudinaryManager;
import GraduationProject.freelancermarket.core.adapters.image.ImageService;
import GraduationProject.freelancermarket.core.business.BusinessRules;
import GraduationProject.freelancermarket.entities.Advert;
import GraduationProject.freelancermarket.entities.AdvertComment;
import GraduationProject.freelancermarket.model.dto.AdvertAddDto;
import GraduationProject.freelancermarket.model.dto.AdvertSearchFilter;
import GraduationProject.freelancermarket.model.dto.AdvertFilter;
import GraduationProject.freelancermarket.model.dto.AdvertUpdateDto;
import GraduationProject.freelancermarket.repository.AdvertRepository;
import GraduationProject.freelancermarket.service.abstracts.AdvertCommentService;
import GraduationProject.freelancermarket.service.abstracts.AdvertService;
import GraduationProject.freelancermarket.service.abstracts.FreelancerService;
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
public class AdvertManager implements AdvertService {

	private final AdvertRepository advertRepository;
	private final FreelancerService freelancerService;
	private final TokenUserNameAndIdValidationService tokenUserNameAndIdValidationService;
	private final ModelMapper modelMapper;

	@Autowired
	private AdvertCommentService advertCommentService;

	@Override
	public Result add(AdvertAddDto advertAddDto) {
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(advertAddDto.getFreelancerId()));
		if (businessRules != null) {
			return new ErrorResult(businessRules.getMessage());
		}
		Advert advert = new Advert(0, advertAddDto.getFreelancerId(), advertAddDto.getSubCategoryId(),
				advertAddDto.getTitle(), advertAddDto.getPrice(), advertAddDto.getInfo(),
				imageUpload(advertAddDto.getImagePath()), LocalDate.now(), 0.0, null, null, null, null, null);
		advertRepository.save(advert);
		return new SuccessResult("İş ilanı eklendi");
	}

	@Override
	public Result delete(int id) {
		var advert = advertRepository.findById(id).orElse(null);
		if (advert == null) {
			return new ErrorResult("İş ilanı bulunamadı");
		}
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(advert.getFreelancerId()));
		if (businessRules != null) {
			return new ErrorResult(businessRules.getMessage());
		}
		advertRepository.deleteById(id);
		return new SuccessResult("İş ilanı silindi");
	}

	@Override
	public Result update(AdvertUpdateDto advertUpdateDto) {
		var result = advertRepository.findById(advertUpdateDto.getId()).orElse(null);
		if (result == null) {
			return new ErrorResult("İlan bulunamadı");
		}
		var businessRules = BusinessRules.run(userIdAndTokenUserNameVerification(advertUpdateDto.getFreelancerId()));
		if (businessRules != null) {
			return new ErrorResult(businessRules.getMessage());
		}
		Advert advert = modelMapper.map(advertUpdateDto, Advert.class);
		if (advertUpdateDto.getImagePath() == null) {
			advert.setImagePath(result.getImagePath());
		} else {
			advert.setImagePath(imageUpload(advertUpdateDto.getImagePath()));
		}
		advert.setDate(result.getDate());
		advert.setAverageScore(result.getAverageScore());
		advertRepository.save(advert);
		return new SuccessResult("İş ilanı güncellendi");
	}

	@Override
	public DataResult<List<Advert>> getAll() {
		return new SuccessDataResult<List<Advert>>(advertRepository.findAll(), "İş ilanları listelendi");
	}

	@Override
	public DataResult<List<Advert>> getByIdIn(List<Integer> ids) {
		return new SuccessDataResult<List<Advert>>(advertRepository.getByIdIn(ids));
	}

	@Override
	public DataResult<List<Advert>> getByUserName(String userName) {
		var result = advertRepository.getByFreelancer_UserName(userName);
		if (result.size() == 0) {
			return new ErrorDataResult<List<Advert>>("Kullanıcıya ait iş ilanı bulunamadı");
		}
		return new SuccessDataResult<List<Advert>>(result);
	}

	@Override
	public DataResult<List<Advert>> getByFreelancerId(int freelancerId) {
		var result = advertRepository.getByFreelancerId(freelancerId);
		if (result == null) {
			return new ErrorDataResult<List<Advert>>(result, "İş ilanı bulunamadı!");
		}
		return new SuccessDataResult<List<Advert>>(result);
	}

	@Override
	public DataResult<List<Advert>> getBySubCategoryId(int subCategoryId) {
		var result = advertRepository.getBySubCategoryId(subCategoryId);
		if (result == null) {
			return new ErrorDataResult<List<Advert>>(result, "İş ilanı bulunamadı!");
		}
		return new SuccessDataResult<List<Advert>>(result);
	}

	@Override
	public DataResult<List<Advert>> getMostPopularJobAdverts() {
		List<Advert> adverts = advertRepository.findAll();
		List<Advert> mostPopularAdverts = new ArrayList<Advert>();
		for (int i = 0; i < 12; i++) {
			if (i < adverts.size()) {
				mostPopularAdverts.add(adverts.get(i));
			}
		}
		return new SuccessDataResult<List<Advert>>(mostPopularAdverts);
	}

	@Override
	public DataResult<List<Advert>> getByPageNumberAndFilter(int pageNumber, String subCategoryName,
			AdvertFilter advertFilter) {
		Pageable pageable = PageRequest.of(pageNumber - 1, 12);
		Page<Advert> result = advertRepository.getByFilter(advertFilter, subCategoryName, pageable);
		return new SuccessDataResult<List<Advert>>(result.getContent(), String.valueOf(result.getTotalElements()));
	}

	@Override
	public DataResult<List<Advert>> getByPageNumberAndSearchFilter(int pageNumber,
			AdvertSearchFilter advertSearchFilter) {
		Page<Advert> result;
		if (pageNumber != 0) {
			Pageable pageable = PageRequest.of(pageNumber - 1, 12);
			result = advertRepository.getBySearchFilter(advertSearchFilter, pageable);
		} else {
			result = advertRepository.getBySearchFilter(advertSearchFilter, null);
		}
		return new SuccessDataResult<List<Advert>>(result.getContent(), String.valueOf(result.getTotalElements()));
	}

	@Override
	public DataResult<Advert> getById(int id) {
		var result = advertRepository.findById(id).orElse(null);
		if (result == null) {
			return new ErrorDataResult<Advert>(result, "İş ilanı bulunamadı");
		}
		return new SuccessDataResult<Advert>(result, "İş ilanı listelendi");
	}

	public Result userIdAndTokenUserNameVerification(int userId) {
		var result = tokenUserNameAndIdValidationService.userIdAndTokenUserNameVerification(userId);
		if (!result.isSuccess()) {
			return new ErrorResult(result.getMessage());
		}
		return new SuccessResult();
	}

	public String imageUpload(MultipartFile file) {
		ImageService imageService = new CloudinaryManager();
		@SuppressWarnings("unchecked")
		Map<String, String> upload = (Map<String, String>) imageService.uploadImage(file).getData();
		return upload.get("url");
	}

	public Result updateScore(int advertId) {
		var result = advertCommentService.getByAdvertId(advertId).getData();
		Double totalScore = 0.0;
		if (result.size() != 0) {
			for (AdvertComment advertComment : result) {
				totalScore += advertComment.getScore();
			}
			var advert = getById(advertId);
			if (!advert.isSuccess()) {
				return new ErrorResult(advert.getMessage());
			}
			advert.getData().setAverageScore(totalScore / result.size());
			advertRepository.save(advert.getData());
			freelancerService.updateAverageScore(advert.getData().getFreelancer());
		}
		return new SuccessResult("İş ilanının ortalama puanı güncellendi");
	}

}
