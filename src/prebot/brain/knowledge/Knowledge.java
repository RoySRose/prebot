package prebot.brain.knowledge;

import prebot.common.code.Code.KnowlegeStatus;

public abstract class Knowledge {
	
	private KnowlegeStatus status = KnowlegeStatus.WAITING;
	
	public KnowlegeStatus learning() {
		if (status == KnowlegeStatus.WAITING) {
			waiting();
		} else if (status == KnowlegeStatus.VERIFING) {
			verifying();
		} else if (status == KnowlegeStatus.PROVED_TRUE) {
			provedTrue();
		} else if (status == KnowlegeStatus.PROVED_FALSE) {
			provedFalse();
		} else if (status == KnowlegeStatus.WRITE_FILE) {
			writeFile();
		}
		return status;
	}

	public String knowledgeName() {
		return getClass().getSimpleName();
	}

	protected abstract boolean notOccured();

	protected abstract boolean occured();

	protected abstract boolean foundCertainProof();

	protected abstract boolean foundCertainDisproof();

	protected void waiting() {
		if (notOccured())
			status = KnowlegeStatus.ENDED;
		else if (occured())
			status = KnowlegeStatus.VERIFING;
	}

	protected void verifying() {
		if (foundCertainProof())
			status = KnowlegeStatus.PROVED_TRUE;
		else if (foundCertainDisproof())
			status = KnowlegeStatus.PROVED_FALSE;
	}

	protected void provedTrue() {
		status = KnowlegeStatus.WRITE_FILE;
	}

	protected void provedFalse() {
		status = KnowlegeStatus.WRITE_FILE;
	}

	protected void writeFile() {
		// TODO Auto-generated method stub
		status = KnowlegeStatus.ENDED;
	}
}

