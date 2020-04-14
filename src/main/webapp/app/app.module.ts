import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import './vendor';
import { JpasetoSharedModule } from 'app/shared/shared.module';
import { JpasetoCoreModule } from 'app/core/core.module';
import { JpasetoAppRoutingModule } from './app-routing.module';
import { JpasetoHomeModule } from './home/home.module';
import { JpasetoEntityModule } from './entities/entity.module';
// jhipster-needle-angular-add-module-import JHipster will add new module here
import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ErrorComponent } from './layouts/error/error.component';

@NgModule({
  imports: [
    BrowserModule,
    JpasetoSharedModule,
    JpasetoCoreModule,
    JpasetoHomeModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    JpasetoEntityModule,
    JpasetoAppRoutingModule
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, FooterComponent],
  bootstrap: [MainComponent]
})
export class JpasetoAppModule {}
