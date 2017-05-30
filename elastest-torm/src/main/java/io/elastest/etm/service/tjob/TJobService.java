package io.elastest.etm.service.tjob;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.ProjectRepository;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.service.sut.SutService;

@Service
public class TJobService {

	private final DockerExecution dockerExec;

	private final TJobRepository tJobRepo;

	private final TJobExecRepository tJobExecRepo;
	@Autowired
	private SutService sutService;

	public TJobService(DockerExecution dockerExec, TJobRepository tJobRepo, TJobExecRepository tJobExecRepo) {
		super();
		this.dockerExec = dockerExec;
		this.tJobRepo = tJobRepo;
		this.tJobExecRepo = tJobExecRepo;
	}

	public TJob createTJob(TJob tjob) {
		return tJobRepo.save(tjob);
	}

	public void deleteTJob(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		tJobRepo.delete(tJob);
	}

	public List<TJob> getAllTJobs() {
		return tJobRepo.findAll();
	}

	public TJobExecution executeTJob(Long tJobId) {
		TJob tjob = tJobRepo.findOne(tJobId);
		TJobExecution tjobExec = new TJobExecution();
		tjobExec.setTjob(tjob);

		SutExecution sutExec = sutService.createSutExecutionBySut(tjob.getSut());

		try {
			dockerExec.configureDocker();
			dockerExec.startLogstash();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HTTPException(500);
		}
		
		try {
			dockerExec.startTest(tjob.getImageName());
			dockerExec.endExec();
			tjobExec.setResult(TJobExecution.ResultEnum.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			tjobExec.setResult(TJobExecution.ResultEnum.FAILURE);
		}

		tjobExec.setSutExecution(sutExec);
		return tJobExecRepo.save(tjobExec);
	}

	public void deleteTJobExec(Long tJobExecId) {
		TJobExecution tJobExec = tJobExecRepo.findOne(tJobExecId);
		tJobExecRepo.delete(tJobExec);
	}

	public TJob getTJobById(Long tJobId) {
		return tJobRepo.findOne(tJobId);
	}

	public List<TJobExecution> getTJobsExecutionsByTJob(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepo.findByTJob(tJob);
	}

	public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepo.findByIdAndTJob(tJobExecId, tJob);
	}

	public TJob modifyTJob(TJob tJob) throws Exception {
		if (tJobRepo.findOne(tJob.getId()) != null) {
			return tJobRepo.save(tJob);
		} else {
			throw new HTTPException(405);
		}
	}

}