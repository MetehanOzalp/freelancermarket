package GraduationProject.freelancermarket.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import GraduationProject.freelancermarket.model.dto.FreelancerCommentAddDto;
import GraduationProject.freelancermarket.service.abstracts.FreelancerCommentService;
import GraduationProject.freelancermarket.utils.ErrorDataResult;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/freelancerComments")
@RequiredArgsConstructor
public class FreelancerCommentController {

	private final FreelancerCommentService freelancerCommentService;

	@PostMapping("add")
	@PreAuthorize("hasRole('ROLE_EMPLOYER')" + "|| hasRole('ROLE_FREELANCER')")
	public ResponseEntity<?> add(@Valid @RequestBody FreelancerCommentAddDto freelancerCommentAddDto) {
		var result = freelancerCommentService.add(freelancerCommentAddDto);
		if (!result.isSuccess()) {
			return new ResponseEntity<Object>(result, HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("delete")
	@PreAuthorize("hasRole('ROLE_EMPLOYER')" + "|| hasRole('ROLE_FREELANCER')")
	public ResponseEntity<?> delete(@RequestBody int id) {
		var result = freelancerCommentService.delete(id);
		if (!result.isSuccess()) {
			return new ResponseEntity<Object>(result, HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(result);
	}

	@GetMapping("getByFreelancerId")
	public ResponseEntity<?> getByFreelancerId(int id) {
		var result = freelancerCommentService.getByFreelancerId(id);
		if (!result.isSuccess()) {
			return new ResponseEntity<Object>(result, HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(result);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDataResult<Object> handleValidationExceptions(MethodArgumentNotValidException exceptions) {
		Map<String, String> validationErrors = new HashMap<String, String>();
		for (FieldError fieldError : exceptions.getBindingResult().getFieldErrors()) {
			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return new ErrorDataResult<Object>(validationErrors, "Doğrulama hataları");
	}

}
