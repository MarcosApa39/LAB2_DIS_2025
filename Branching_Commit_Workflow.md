# How to Branch and Commit

## Branching Strategy and Workflow

### **Main Branches**
These are the main branches used for specific milestones or tasks. Feature branches will be created from these branches to work on individual features or fixes.

1. **`master`**:
   - The primary branch with stable, production-ready code.
   - Only merge completed, tested, and reviewed work here.

2. **`setup-apps-frontend-backend`**:
   - Temporary branch for initial project setup.
   - Contains Spring Boot and Vaadin configuration.
   - Merged into `master` after setup, then deleted.

3. **`backend`**:
   - Main branch for backend development.
   - Use for all backend-related tasks, such as API endpoints and unit tests.
   - Feature branches (e.g., `feature/add-crud-endpoints`) should be created for specific backend tasks.

4. **`frontend`**:
   - Main branch for frontend development using Vaadin.
   - Use for all UI-related tasks, such as grids and filters.
   - Feature branches (e.g., `feature/add-datepicker-filter`) should be created for specific frontend tasks.

5. **`dockerization`**:
   - Main branch for Docker-related work.
   - Use for creating and testing Dockerfiles for backend and frontend applications.
   - Feature branches (e.g., `feature/backend-dockerfile`) should be created for specific tasks.

6. **`ci-cd`**:
   - Main branch for CI/CD pipeline setup and testing.
   - Use for deployment and integration tasks.
   - Feature branches (e.g., `feature/setup-ci-pipeline`) should be created for specific tasks.

---

### **Feature Branches**
Feature branches are created from the main branches (`setup-apps-frontend-backend`, `backend`, `frontend`, `dockerization`, and `ci-cd`) to isolate work on individual tasks or features. Once the work is complete, the feature branch is merged back into the relevant main branch and then deleted.

#### **Branch Naming Convention**:
Within the neccesary main branch, use clear, descriptive names for feature branches:
- **New Features**: `feature/[task-description]`
  - Example: `feature/add-crud-endpoints`, `feature/vaadin-grid-crud`.
- **Bug Fixes**: `bugfix/[task-description]`
  - Example: `bugfix/fix-docker-ports`.
- **Hotfixes**: `hotfix/[task-description]`
  - Example: `hotfix/update-docker-image`.

---

### **Branch Creation and Workflow**

1. **Create a New Branch**:
   - Create a feature branch from the relevant main branch:
     ```bash
     git checkout -b feature/[task-name] [main-branch]
     ```
     Example:
     ```bash
     git checkout -b feature/add-crud-endpoints backend
     ```

2. **Work on the Feature**:
   - Add and commit your changes regularly:
     ```bash
     git add [file-name]
     git commit -m "feat: [description of the feature]"
     ```

3. **Push the Branch to Remote**:
   - Push your branch to the remote repository for collaboration:
     ```bash
     git push origin feature/[task-name]
     ```

4. **Create a Pull Request (PR)**:
   - Open a PR to merge your feature branch into the relevant main branch.
   - Provide a descriptive title and include details of the work done.

5. **Review and Merge**:
   - Team members review the PR, suggest changes if necessary, and approve it.
   - Once approved, merge the branch:
     ```bash
     git checkout [main-branch]
     git merge feature/[task-name]
     ```

6. **Delete the Feature Branch**:
   - After merging, delete the branch locally and remotely:
     ```bash
     git branch -d feature/[task-name]
     git push origin --delete feature/[task-name]
     ```

---

### General Rules
1. **Never Commit Directly to Main Branches**:
- Always work on a feature branch.

2. **Keep Feature Branches Short-Lived**:
- Merge completed tasks promptly to avoid conflicts.

3. **Update Your Branch**:
- Regularly pull updates from the main branch to stay aligned:
  ```bash
  git pull origin [main-branch]
  ```
  
4. **Communicate**:
- Use PR descriptions and GitHub issues to keep the team informed.
