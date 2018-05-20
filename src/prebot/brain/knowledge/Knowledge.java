package prebot.brain.knowledge;

import prebot.common.code.Code.KnowlegeStatus;
import prebot.common.util.TimeUtils;

public abstract class Knowledge {
	
	private StringBuilder knowlegdeResult = new StringBuilder("WAITING");

	private KnowlegeStatus status = KnowlegeStatus.WAITING;

	public KnowlegeStatus getStatus() {
		return status;
	}

	public boolean learning() {
		if (status == KnowlegeStatus.WAITING) {
			return waiting();
		} else if (status == KnowlegeStatus.VERIFING) {
			return verifying();
		} else if (status == KnowlegeStatus.PROVED_TRUE) {
			return provedTrue();
		} else if (status == KnowlegeStatus.PROVED_FALSE) {
			return provedFalse();
		} else if (status == KnowlegeStatus.WRITE_FILE) {
			return writeFile();
		} else {
			return false;
		}
	}

	public String knowledgeName() {
		return getClass().getSimpleName();
	}

	protected abstract boolean notOccured();

	protected abstract boolean occured();

	protected abstract boolean foundCertainProof();

	protected abstract boolean foundCertainDisproof();

	protected boolean waiting() {
		if (notOccured()) {
			status = KnowlegeStatus.ENDED;
			knowlegdeResult.append(" -> ENDED");
			return true;
		} else if (occured()) {
			status = KnowlegeStatus.VERIFING;
			knowlegdeResult.append(" -> VERIFING");
			return true;
		} else {
			return false;
		}
	}

	protected boolean verifying() {
		if (foundCertainProof()) {
			status = KnowlegeStatus.PROVED_TRUE;
			knowlegdeResult.append(" -> PROVED_TRUE");
			return true;
		} else if (foundCertainDisproof()) {
			status = KnowlegeStatus.PROVED_FALSE;
			knowlegdeResult.append(" -> PROVED_FALSE");
			return true;
		} else {
			return false;
		}
	}

	protected boolean provedTrue() {
		status = KnowlegeStatus.WRITE_FILE;
		knowlegdeResult.append(" -> WRITE_FILE");
		return true;
	}

	protected boolean provedFalse() {
		status = KnowlegeStatus.WRITE_FILE;
		knowlegdeResult.append(" -> WRITE_FILE");
		return true;
	}

	protected boolean writeFile() {
//		FileUtils.appendTextToFile(knowledgeName() + ".txt", knowlegdeResult.toString());
		status = KnowlegeStatus.ENDED;
		knowlegdeResult.append(" -> ENDED ... ").append(TimeUtils.elapsedFrames()).append("\r\n");
		return false;
	}
}
