# Data Mapping Wizard

A visual tool for creating and configuring data mappings between database tables.

## Project Structure

The application has been refactored into a modular design with these key components:

### Core Components

- **DataMapWizard**: Main application window that coordinates the UI
- **DataMappingModel**: Manages all data model operations and state
- **WizardNavigationController**: Handles panel navigation and button states
- **PanelManager**: Manages wizard panels and their visibility
- **DatabaseOperationsManager**: Handles database connections and queries
- **PanelRefreshManager**: Refreshes UI components to match data model state

### UI Panels

- **DatabaseConfigPanel**: Configure data sources
- **AddTablesPanel**: Select source and target tables
- **AddColumnsPanel**: Define columns for selected tables
- **NoneMappingPanel**: Create direct column mappings
- **DictMappingPanel**: Create dictionary-based mappings
- **ConstantMappingPanel**: Set constant value mappings
- **ExternalConnectionPanel**: Configure join-based mappings
- **GenerateCodePanel**: Review and save generated code

## Getting Started

1. Configure your data sources
2. Add source and target tables
3. Define columns for each table
4. Create mappings between columns
5. Generate and save your mapping code

## Development

To add new functionality:

1. Add model classes in the `com.datamap.model` package
2. Add UI components in the `com.datamap.ui.panels` package
3. Add controller logic in the `com.datamap.ui.controller` package