-- Public-site CMS content (admin-manageable, see FrontendController), CRM tasks, and misc gateway ledgers.
-- Excluded from this port: `adverts` and `orders_p2ps` (present in the source DB dump but with zero
-- controller/route references anywhere in the app — dead schema, not a reachable feature) and the
-- `themes` table (superseded by the `dashboard_style` dark/light column already on users/admins).

CREATE TABLE contents (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ref_key     VARCHAR(191) NOT NULL,
    title       VARCHAR(191) NOT NULL,
    description TEXT NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_contents_ref_key (ref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cms_images (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ref_key     VARCHAR(191) NOT NULL,
    title       VARCHAR(191) NOT NULL,
    description TEXT NOT NULL,
    image_path  VARCHAR(191) NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_cms_images_ref_key (ref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE faqs (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ref_key    VARCHAR(191) NULL,
    question   VARCHAR(500) NULL,
    answer     TEXT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE testimonies (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ref_key      VARCHAR(191) NULL,
    position     VARCHAR(191) NULL,
    name         VARCHAR(191) NULL,
    what_is_said VARCHAR(1000) NULL,
    picture      VARCHAR(191) NULL,
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE terms_privacies (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    description TEXT NOT NULL,
    use_terms   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CRM: tasks, assignable to admins/agents. "Leads" are `users` with customer_status='Lead'
-- (converted to 'Customer' on conversion) — there is no separate leads table in the source app.
CREATE TABLE tasks (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(191) NULL,
    status       VARCHAR(191) NOT NULL DEFAULT 'pending',
    note         TEXT NULL,
    assigned_to_admin_id BIGINT UNSIGNED NULL COMMENT 'legacy `designation`',
    start_date   DATE NULL,
    end_date     DATE NULL,
    priority     VARCHAR(191) NULL,
    attachment   VARCHAR(191) NULL,
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_admin FOREIGN KEY (assigned_to_admin_id) REFERENCES admins(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE binance_pay_transactions (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NULL,
    prepay_id   VARCHAR(191) NULL,
    deposit_id  BIGINT UNSIGNED NULL,
    type        VARCHAR(191) NULL,
    status      VARCHAR(191) NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_binance_pay_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_binance_pay_deposit FOREIGN KEY (deposit_id) REFERENCES deposits(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
