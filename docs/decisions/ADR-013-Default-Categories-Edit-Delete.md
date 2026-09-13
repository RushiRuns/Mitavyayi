# ADR-013: Allow Deletion and Editing of Default Categories

## Status
Accepted

## Context
Mitavyay ships with 11 default categories (Food & Dining, Groceries, Transport, Bills & Utilities, Shopping, Healthcare, Entertainment, Salary & Income, Investments, Transfer, and Other).
Previously, default categories were locked:
1. The UI hid the Edit and Delete buttons on `CategoriesScreen` for any category where `isCustom == false`.
2. `CategoryRepository.deleteCategory()` and `canDeleteCategory()` explicitly checked `isCustom` and blocked deletion of default categories.
3. `CategoryRepository.updateCustomCategory()` returned an error when attempting to update default categories.
4. Auto-seeding logic in `CategoryRepositoryImpl.getAllCategories()` and `QuickAddExpenseViewModel` re-inserted all 11 default categories whenever the category list was empty, meaning deleted categories would immediately reappear on app launch.

Users need the ability to customize their financial taxonomy to reflect their own lifestyle, including editing or deleting categories they do not use, without default categories resurrecting on app restart.

## Decision
1. **Full CRUD for All Categories**:
   - Both default and custom categories can now be edited (name, color, icon) and deleted.
   - Removed `isCustom` checks from `CategoryRepository.deleteCategory()`, `canDeleteCategory()`, and `updateCustomCategory()`.
   - In `CategoriesScreen.kt`, Edit and Delete action icons are displayed for all categories. Default categories retain a subtle "Default" badge chip for user orientation.

2. **Deletion Warning & Confirmation**:
   - Before deleting any category, an alert dialog is presented:
     - Title: "Delete Category?"
     - Text: "Transactions with this category will keep the category name but may appear uncategorized."
     - Buttons: "Delete" (destructive) and "Cancel".
   - Because transactions store the category as a plain String name, existing transaction records remain valid and intact.

3. **Seeding Prevention via DataStore Preference**:
   - Added `has_seeded_default_categories` boolean preference to Jetpack DataStore (`PreferenceKeys.HAS_SEEDED_DEFAULT_CATEGORIES`).
   - Default categories are only seeded once on initial app installation (via Room's `Callback.onCreate()` or `CategoryRepository.seedDefaultCategories()`).
   - `CategoryRepositoryImpl.getAllCategories()` checks `hasSeededDefaultCategories`: if already seeded, it never re-inserts defaults even if the categories table is empty.
   - Removed redundant auto-seeding `init` block from `QuickAddExpenseViewModel`.

4. **AddCategoryDialog Edit Mode**:
   - `AddCategoryDialog` supports both add and edit modes via optional `categoryToEdit` parameter.
   - When `categoryToEdit` is provided, fields are pre-filled, the title displays "Edit Category", and the submit button displays "Save Changes".
   - When null, an empty form is displayed with title "Add Category" and submit button "Add Category".

## Consequences
- Users have complete control over category management.
- Deleted categories do not reappear across app restarts or background process kills.
- Historical transaction integrity is preserved.
