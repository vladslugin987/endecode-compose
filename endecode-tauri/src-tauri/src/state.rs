use std::{
    collections::HashMap,
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc, Mutex,
    },
};

#[derive(Clone, Default)]
pub struct AppState {
    jobs: Arc<Mutex<HashMap<String, Arc<AtomicBool>>>>,
}

impl AppState {
    pub fn register_job(&self, job_id: &str) {
        if let Ok(mut jobs) = self.jobs.lock() {
            jobs.insert(job_id.to_string(), Arc::new(AtomicBool::new(false)));
        }
    }

    pub fn cancel_job(&self, job_id: &str) -> bool {
        if let Ok(jobs) = self.jobs.lock()
            && let Some(flag) = jobs.get(job_id)
        {
            flag.store(true, Ordering::Relaxed);
            return true;
        }
        false
    }

    pub fn is_cancelled(&self, job_id: &str) -> bool {
        if let Ok(jobs) = self.jobs.lock()
            && let Some(flag) = jobs.get(job_id)
        {
            return flag.load(Ordering::Relaxed);
        }
        false
    }

    pub fn finish_job(&self, job_id: &str) {
        if let Ok(mut jobs) = self.jobs.lock() {
            jobs.remove(job_id);
        }
    }
}
