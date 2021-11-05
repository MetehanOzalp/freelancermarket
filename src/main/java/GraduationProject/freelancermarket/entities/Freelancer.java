package GraduationProject.freelancermarket.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "freelancers")
@PrimaryKeyJoinColumn(name = "user_id")
@AllArgsConstructor
@NoArgsConstructor
public class Freelancer extends User {

	@Column(name = "about")
	private String about;

	@Column(name = "image_path")
	private String imagePath;

	@OneToMany(mappedBy = "freelancer")
	private List<Skill> skills;

	@OneToMany(mappedBy = "freelancer")
	private List<Advert> adverts;

	@OneToMany(mappedBy = "freelancer")
	private List<FreelancerComment> freelancerComments;

}
