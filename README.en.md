# Enterprise Online Employee HR Management Information System
#### Description
With the expansion of enterprise‑scale and frequent personnel mobility, traditional personnel management relying on Excel spreadsheets and paper archives brings many drawbacks: scattered data, low efficiency of query and statistics, risk of file loss and disordered permission control. HR staff spend plenty of time on repetitive work such as file sorting, contract expiration reminder, salary and attendance statistics. Ordinary employees cannot check their personal archives, contracts, salary and attendance information by themselves.

This system is a B/S‑architecture comprehensive human‑resource management platform. It covers business modules including department‑post management, employee files, labor contracts, personnel transfer, salary, attendance and account permission management.
Three user roles are designed: **System Administrator, HR Specialist, Ordinary Employee**. Fine‑grained permission isolation is implemented. Sensitive information such as ID number and salary will be desensitized. All personnel‑change operations are audited and logged to meet personal‑information‑protection compliance requirements.

Team Members: Yu Yue, Yang Jiaying, Wang Yan
> Intern role rotation: Fixed roles are assigned in Week 1. Role rotation starts from Week 2 and completes in Week 4. Each member shall experience at least two different roles.

#### Software Architecture
- Back‑end: SpringBoot + SpringMVC + MyBatis, JDK 11
- Front‑end: Vue3 + Element‑Plus
- Database: MySQL 8.0
- Project layers: Controller layer, Service layer, Mapper layer, Entity layer, Config layer
- Repository directory structure
```
EOPMIS/
├── docs/                           # All internship documents
│   ├── prd/                        # Product requirement document
│   ├── design/                     # High‑level & detailed design, ER‑diagram, SQL scripts
│   ├── test/                       # Test cases and test reports
│   └── vibe‑log/                   # AI prompt logs and role‑rotation records
├── backend/                        # SpringBoot back‑end project
├── frontend/                       # Vue3 front‑end project
├── README.md
├── PROJECT.md                      # Vibe‑Coding AI context file
└── .gitignore                      # Git ignore rules
```

#### Git Branch Strategy
- `master`: Main branch for stable releases. **Direct commits are forbidden**. Accept merge requests only from dev branch.
- `dev`: Development branch. All group documents and code development are finished on this branch.

#### Installation
> For post‑skeleton phase only; current status: repository initialization
1. Back‑end: Enter `backend` folder, modify database configuration in `application‑dev.yml`, run `EopmisApplication`.
2. Front‑end: Enter `frontend` folder, run `npm install`, then run `npm run dev`.

#### Instructions
1. System Administrator: Create accounts and assign role permissions, not responsible for HR business data.
2. HR Specialist: Maintain departments, employee files, contracts, transfers, salary and attendance records.
3. Ordinary Employee: View personal archives, contracts, salary and attendance; only allowed to modify self login password.

#### Contribution
1. All development and document work shall be performed on the `dev` branch.
2. Pull the latest remote content before modification to avoid conflicts.
3. Write clear commit messages to describe your changes.
4. Submit Pull Request from dev to master after functions are fully developed and tested.

#### Current Project Status
> Week 1: Git repository initialized with empty project skeleton. Business functions are not implemented. Product requirement document is stored under `docs/prd/`.
