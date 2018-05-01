package prebot.brain.knowledge;

import prebot.common.code.Code.KnowlegeStatus;

public abstract class Knowledge {

	private KnowlegeStatus status = KnowlegeStatus.WAITING;

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
			return true;
		} else if (occured()) {
			status = KnowlegeStatus.VERIFING;
			return true;
		} else {
			return false;
		}
	}

	protected boolean verifying() {
		if (foundCertainProof()) {
			status = KnowlegeStatus.PROVED_TRUE;
			return true;
		} else if (foundCertainDisproof()) {
			status = KnowlegeStatus.PROVED_FALSE;
			return true;
		} else {
			return false;
		}
	}

	protected boolean provedTrue() {
		status = KnowlegeStatus.WRITE_FILE;
		return true;
	}

	protected boolean provedFalse() {
		status = KnowlegeStatus.WRITE_FILE;
		return true;
	}

	protected boolean writeFile() {
		// TODO Auto-generated method stub
		status = KnowlegeStatus.ENDED;
		return false;
	}
}
